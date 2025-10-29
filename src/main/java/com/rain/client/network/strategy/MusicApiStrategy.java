package com.rain.client.network.strategy;

import com.rain.client.model.Lyric;
import com.rain.client.model.SearchResult;

import java.util.concurrent.CompletableFuture;

/**
 * 音乐API策略接口
 *
 * @author 落雨川
 * @version 1.7
 * @since 1.7
 */
public interface MusicApiStrategy {

    /**
     * 搜索音乐
     *
     * @param query 搜索关键词
     * @return 搜索结果的CompletableFuture
     */
    CompletableFuture<SearchResult> searchMusic(String query);

    /**
     * 获取歌词
     *
     * @param trackId 音轨ID
     * @return 歌词的CompletableFuture
     */
    CompletableFuture<Lyric> getLyric(String trackId);

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();
}