package com.rain.manager;

import com.rain.MusicPlayerMod;
import com.rain.model.MusicTrack;
import com.rain.util.CollUtil;

import java.util.*;

/**
 * 分类管理器
 * <p>
 * 管理用户自定义的歌曲分类
 * </p>
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class CategoryManager {

    /**
     * 最大分类数量限制
     */
    private static final int MAX_CATEGORIES = 5;

    /**
     * 分类列表
     */
    private final Map<String, List<MusicTrack>> categories;

    /**
     * 数据持久化管理器
     */
    private final DataPersistenceManager persistenceManager;

    public CategoryManager(DataPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.categories = new LinkedHashMap<>(MAX_CATEGORIES * 2);
        loadCategories();
        MusicPlayerMod.LOGGER.info("分类管理器已初始化");
    }

    /**
     * 创建新分类
     */
    public boolean createCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            MusicPlayerMod.LOGGER.warn("分类名称不能为空");
            return false;
        }
        // 检查是否超过数量限制
        if (categories.size() >= MAX_CATEGORIES) {
            MusicPlayerMod.LOGGER.warn("分类数量已达到上限（{}个）", MAX_CATEGORIES);
            return false;
        }
        String trimmedName = categoryName.trim();
        if (categories.containsKey(trimmedName)) {
            MusicPlayerMod.LOGGER.warn("分类已存在: {}", trimmedName);
            return false;
        }
        categories.put(trimmedName, new ArrayList<>());
        saveCategories();
        MusicPlayerMod.LOGGER.info("已创建分类: {}", trimmedName);
        return true;
    }

    /**
     * 删除分类
     */
    public boolean deleteCategory(String categoryName) {
        if (categories.remove(categoryName) != null) {
            saveCategories();
            MusicPlayerMod.LOGGER.info("已删除分类: {}", categoryName);
            return true;
        }
        return false;
    }

    /**
     * 添加歌曲到分类
     */
    public boolean addTrackToCategory(String categoryName, MusicTrack track) {
        if (Objects.isNull(track)) {
            return false;
        }
        MusicPlayerMod.LOGGER.warn("准备添加到分类: {},{}", categoryName, track.getTitle());
        List<MusicTrack> tracks = categories.get(categoryName);
        if (tracks == null) {
            MusicPlayerMod.LOGGER.warn("分类不存在: {}", categoryName);
            return false;
        }
        for (MusicTrack existing : tracks) {
            if (existing.getId().equals(track.getId())) {
                MusicPlayerMod.LOGGER.warn("歌曲已在分类中: {}", track.getTitle());
                return false;
            }
        }
        tracks.add(track);
        saveCategories();
        MusicPlayerMod.LOGGER.info("已添加歌曲到分类 [{}]: {}", categoryName, track.getTitle());
        return true;
    }

    /**
     * 从分类中移除歌曲
     */
    public boolean removeTrackFromCategory(String categoryName, String trackId) {
        List<MusicTrack> tracks = categories.get(categoryName);
        if (Objects.isNull(tracks)) {
            return false;
        }
        boolean removed = tracks.removeIf(track -> track.getId().equals(trackId));
        if (removed) {
            saveCategories();
            MusicPlayerMod.LOGGER.info("已从分类 [{}] 移除歌曲", categoryName);
        }
        return removed;
    }

    /**
     * 从分类中移除歌曲（通过索引）
     */
    public boolean removeTrackFromCategory(String categoryName, int index) {
        List<MusicTrack> tracks = categories.get(categoryName);
        if (tracks == null || index < 0 || index >= tracks.size()) {
            return false;
        }
        MusicTrack removed = tracks.remove(index);
        saveCategories();
        MusicPlayerMod.LOGGER.info("已从分类 [{}] 移除歌曲: {}", categoryName, removed.getTitle());
        return true;
    }

    /**
     * 获取分类中的所有歌曲
     */
    public List<MusicTrack> getTracksInCategory(String categoryName) {
        List<MusicTrack> tracks = categories.get(categoryName);
        return CollUtil.isEmpty(tracks) ? Collections.emptyList() :
                Collections.unmodifiableList(tracks);
    }

    /**
     * 获取所有分类名称
     */
    public Set<String> getCategoryNames() {
        return Collections.unmodifiableSet(categories.keySet());
    }

    /**
     * 获取分类数量
     */
    public int getCategoryCount() {
        return categories.size();
    }

    /**
     * 获取最大分类数量限制
     */
    public int getMaxCategories() {
        return MAX_CATEGORIES;
    }

    /**
     * 检查是否已达到分类数量上限
     */
    public boolean isAtMaxCapacity() {
        return categories.size() >= MAX_CATEGORIES;
    }

    /**
     * 检查分类是否存在
     */
    public boolean hasCategory(String categoryName) {
        return categories.containsKey(categoryName);
    }

    /**
     * 获取分类中的歌曲数量
     */
    public int getTrackCountInCategory(String categoryName) {
        List<MusicTrack> tracks = categories.get(categoryName);
        return CollUtil.isEmpty(tracks) ? 0 : tracks.size();
    }

    /**
     * 清空所有分类
     */
    public void clearAllCategories() {
        categories.clear();
        saveCategories();
        MusicPlayerMod.LOGGER.info("所有分类已清空");
    }

    /**
     * 保存分类
     */
    private void saveCategories() {
        persistenceManager.saveCategories(categories);
    }

    /**
     * 加载分类
     */
    private void loadCategories() {
        Map<String, List<MusicTrack>> loaded = persistenceManager.loadCategories();
        categories.clear();
        categories.putAll(loaded);
    }

    /**
     * 获取所有分类
     */
    public Map<String, List<MusicTrack>> getAllCategories() {
        Map<String, List<MusicTrack>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<MusicTrack>> entry : categories.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }
}
