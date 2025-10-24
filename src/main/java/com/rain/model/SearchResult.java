package com.rain.model;

import java.util.List;

/**
 * 搜索结果记录
 *
 * @param query        keywords
 * @param tracks       音轨列表
 * @param totalResults 总结果数
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public record SearchResult(String query, List<MusicTrack> tracks, int totalResults) {

    /**
     * 判断搜索结果是否为空
     *
     * @return 如果没有搜索到任何音轨返回true
     */
    public boolean isEmpty() {
        return tracks.isEmpty();
    }
}
