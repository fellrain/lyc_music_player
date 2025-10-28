package com.rain.client.manager;

import com.rain.client.MusicPlayerClientMod;
import com.rain.client.model.MusicTrack;

import java.util.*;

/**
 * 收藏管理器
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public final class FavoriteManager {

    /**
     * 收藏的歌曲ID
     */
    private final Set<String> favoriteIds;

    /**
     * 收藏的歌曲
     */
    private final Map<String, MusicTrack> favoriteTracks;

    /**
     * 数据持久化管理器
     */
    private final DataPersistenceManager persistenceManager;

    public FavoriteManager(DataPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.favoriteIds = new HashSet<>(128);
        this.favoriteTracks = new LinkedHashMap<>(128);
        loadFavorites();
        MusicPlayerClientMod.LOGGER.info("收藏管理器已初始化");
    }

    /**
     * 添加收藏
     */
    public boolean addFavorite(MusicTrack track) {
        if (Objects.isNull(track)) {
            return false;
        }
        boolean added = favoriteIds.add(track.getId());
        if (added) {
            favoriteTracks.put(track.getId(), track);
            saveFavorites();
            MusicPlayerClientMod.LOGGER.info("已添加收藏: {}", track.getTitle());
        }
        return added;
    }

    /**
     * 移除收藏
     */
    public boolean removeFavorite(String trackId) {
        boolean removed = favoriteIds.remove(trackId);
        if (removed) {
            favoriteTracks.remove(trackId); // 同时移除歌曲对象
            saveFavorites();
            MusicPlayerClientMod.LOGGER.info("已移除收藏: {}", trackId);
        }
        return removed;
    }

    /**
     * 移除收藏（通过MusicTrack）
     */
    public boolean removeFavorite(MusicTrack track) {
        if (Objects.isNull(track)) {
            return false;
        }
        return removeFavorite(track.getId());
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(String trackId) {
        return favoriteIds.contains(trackId);
    }

    /**
     * 检查是否已收藏（通过MusicTrack）
     */
    public boolean isFavorite(MusicTrack track) {
        return !Objects.isNull(track) && isFavorite(track.getId());
    }

    /**
     * 切换收藏状态
     * 
     * @return true表示已收藏，false表示已取消收藏
     */
    public boolean toggleFavorite(MusicTrack track) {
        if (isFavorite(track)) {
            removeFavorite(track);
            return false;
        } else {
            addFavorite(track);
            return true;
        }
    }

    /**
     * 获取所有收藏的ID
     */
    public Set<String> getFavoriteIds() {
        return Collections.unmodifiableSet(favoriteIds);
    }

    /**
     * 获取所有收藏的歌曲列表
     */
    public List<MusicTrack> getFavoriteTracks() {
        return new ArrayList<>(favoriteTracks.values());
    }

    /**
     * 获取收藏数量
     */
    public int getFavoriteCount() {
        return favoriteIds.size();
    }

    /**
     * 清空所有收藏
     */
    public void clearFavorites() {
        favoriteIds.clear();
        favoriteTracks.clear();
        saveFavorites();
        MusicPlayerClientMod.LOGGER.info("收藏列表已清空");
    }

    /**
     * 保存收藏列表
     */
    private void saveFavorites() {
        persistenceManager.saveFavoriteTracks(new ArrayList<>(favoriteTracks.values()));
    }

    /**
     * 加载收藏列表
     */
    private void loadFavorites() {
        List<MusicTrack> loaded = persistenceManager.loadFavoriteTracks();
        favoriteIds.clear();
        favoriteTracks.clear();
        for (MusicTrack track : loaded) {
            favoriteIds.add(track.getId());
            favoriteTracks.put(track.getId(), track);
        }
    }
}
