package com.rain.client.manager;

import com.rain.client.MusicPlayerClientMod;
import com.rain.client.audio.AudioManager;
import com.rain.client.model.MusicTrack;
import com.rain.common.config.ModConfig;
import com.rain.common.util.CollUtil;

import java.util.*;

/**
 * 音乐管理器
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class MusicManager {

    private final AudioManager audioManager;

    private final List<MusicTrack> playlist;

    private int currentIndex;

    private PlaybackMode playbackMode;

    private final Random random;

    private DataPersistenceManager persistenceManager;

    /**
     * 构造音乐管理器
     *
     * @param audioManager 音频管理器
     */
    public MusicManager(AudioManager audioManager) {
        this.audioManager = audioManager;
        this.playlist = new ArrayList<>();
        this.currentIndex = -1;
        this.playbackMode = PlaybackMode.SEQUENTIAL;
        this.random = new Random();
        MusicPlayerClientMod.LOGGER.info("音乐管理器已初始化");
    }

    /**
     * 初始化持久化管理器
     */
    public void initialize(DataPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        loadPlaylist();
    }

    /**
     * 添加音轨到播放列表
     */
    public boolean addToPlaylist(MusicTrack track) {
        if (playlist.size() >= ModConfig.MAX_PLAYLIST_SIZE) {
            MusicPlayerClientMod.LOGGER.warn("播放列表已满");
            return false;
        }
        // 检查是否已存在（根据歌曲ID）
        for (MusicTrack existingTrack : playlist) {
            if (existingTrack.getId().equals(track.getId())) {
                MusicPlayerClientMod.LOGGER.info("歌曲已在播放列表中: {}", track.getTitle());
                return false;
            }
        }
        playlist.add(track);
        savePlaylist();
        MusicPlayerClientMod.LOGGER.info("已添加到播放列表: {}", track.getTitle());
        return true;
    }

    /**
     * 从播放列表移除音轨
     */
    public boolean removeFromPlaylist(int index) {
        if (index < 0 || index >= playlist.size()) {
            return false;
        }
        playlist.remove(index);
        if (currentIndex >= index && currentIndex > 0) {
            currentIndex--;
        }
        savePlaylist();
        return true;
    }

    /**
     * 清空播放列表
     */
    public void clearPlaylist() {
        playlist.clear();
        currentIndex = -1;
        savePlaylist();
        MusicPlayerClientMod.LOGGER.info("播放列表已清空");
    }

    /**
     * 播放指定索引的音轨
     */
    public void playTrackAt(int index) {
        if (index < 0 || index >= playlist.size()) {
            MusicPlayerClientMod.LOGGER.warn("无效的播放列表索引: {}", index);
            return;
        }
        currentIndex = index;
        MusicTrack track = playlist.get(currentIndex);
        audioManager.playTrack(track);
        savePlaylist();
    }

    /**
     * 播放下一首音乐
     */
    public void playNext() {
        if (CollUtil.isEmpty(playlist)) {
            MusicPlayerClientMod.LOGGER.warn("播放列表为空");
            return;
        }
        switch (playbackMode) {
            case SEQUENTIAL:
                currentIndex = (currentIndex + 1) % playlist.size();
                break;
            case SHUFFLE:
                currentIndex = random.nextInt(playlist.size());
                break;
            case REPEAT_ONE:
                // 单曲循环，保持当前索引
                break;
            case REPEAT_ALL:
                currentIndex = (currentIndex + 1) % playlist.size();
                break;
        }
        playTrackAt(currentIndex);
    }

    /**
     * 播放上一首音乐
     */
    public void playPrevious() {
        if (CollUtil.isEmpty(playlist)) {
            MusicPlayerClientMod.LOGGER.warn("播放列表为空");
            return;
        }
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        playTrackAt(currentIndex);
    }

    /**
     * 随机打乱播放列表
     */
    public void shuffle() {
        if (CollUtil.isEmpty(playlist)) {
            return;
        }
        MusicTrack currentTrack = currentIndex >= 0 && currentIndex < playlist.size() ?
                                  playlist.get(currentIndex) : null;
        Collections.shuffle(playlist);
        if (!Objects.isNull(currentTrack)) {
            currentIndex = playlist.indexOf(currentTrack);
        }
        savePlaylist();
        MusicPlayerClientMod.LOGGER.info("播放列表已随机排序");
    }

    /**
     * 获取播放列表的副本
     */
    public List<MusicTrack> getPlaylist() {
        return Collections.unmodifiableList(playlist);
    }

    /**
     * 获取当前播放索引
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * 获取播放列表大小
     */
    public int getPlaylistSize() {
        return playlist.size();
    }

    /**
     * 判断播放列表是否为空
     */
    public boolean isPlaylistEmpty() {
        return CollUtil.isEmpty(playlist);
    }

    /**
     * 获取当前播放模式
     */
    public PlaybackMode getPlaybackMode() {
        return playbackMode;
    }

    /**
     * 循环切换播放模式
     */
    public PlaybackMode cyclePlaybackMode() {
        PlaybackMode[] modes = PlaybackMode.values();
        int nextIndex = (playbackMode.ordinal() + 1) % modes.length;
        playbackMode = modes[nextIndex];
        MusicPlayerClientMod.LOGGER.info("播放模式已切换为: {}", playbackMode);
        return playbackMode;
    }

    /**
     * 保存播放列表
     */
    private void savePlaylist() {
        if (!Objects.isNull(persistenceManager)) {
            persistenceManager.savePlaylist(playlist, currentIndex);
        }
    }

    /**
     * 加载播放列表
     */
    private void loadPlaylist() {
        if (Objects.isNull(persistenceManager)) {
            return;
        }
        Map<String, Object> data = persistenceManager.loadPlaylist();
        if (Objects.isNull(data)) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<MusicTrack> tracks = (List<MusicTrack>) data.get("tracks");
        Integer savedIndex = (Integer) data.get("currentIndex");
        if (!Objects.isNull(tracks)) {
            playlist.clear();
            playlist.addAll(tracks);
        }
        if (!Objects.isNull(savedIndex)) {
            currentIndex = savedIndex;
        }
        MusicPlayerClientMod.LOGGER.info("播放列表已从本地加载，当前索引: {}", currentIndex);
    }
}