package com.rain.client.network;

import com.rain.client.MusicPlayerClientMod;
import com.rain.client.model.Lyric;
import com.rain.client.model.SearchResult;
import com.rain.client.network.strategy.MusicApiStrategy;
import com.rain.client.network.strategy.MusicApiStrategyFactory;
import com.rain.common.enums.MusicStrategyEnum;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 音乐API客户端
 *
 * @author 落雨川
 * @version 1.7
 * @since 1.0
 */
public final class MusicAPIClient {

    private final MusicApiStrategyFactory strategyFactory;

    public MusicAPIClient() {
        this.strategyFactory = MusicApiStrategyFactory.getInstance();
        MusicPlayerClientMod.LOGGER.info("MusicAPIClient 初始化，使用策略工厂");
    }

    /**
     * 搜索音乐
     */
    public CompletableFuture<SearchResult> searchMusic(String query) {
        MusicApiStrategy currentStrategy = strategyFactory.getCurrentStrategy();
        if (!Objects.isNull(currentStrategy)) {
            MusicPlayerClientMod.LOGGER.info("使用 {} 策略搜索音乐: {}", currentStrategy.getStrategyName(), query);
            return currentStrategy.searchMusic(query);
        } else {
            MusicPlayerClientMod.LOGGER.error("没有可用的音乐API策略");
            return CompletableFuture.completedFuture(new SearchResult(query, List.of(), 0));
        }
    }

    /**
     * 获取歌词
     */
    public CompletableFuture<Lyric> getLyric(String trackId) {
        MusicApiStrategy currentStrategy = strategyFactory.getCurrentStrategy();
        if (currentStrategy != null) {
            MusicPlayerClientMod.LOGGER.info("使用 {} 策略获取歌词: {}", currentStrategy.getStrategyName(), trackId);
            return currentStrategy.getLyric(trackId);
        } else {
            MusicPlayerClientMod.LOGGER.error("没有可用的音乐API策略");
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 设置当前使用的API策略
     *
     * @param strategyName 策略名称
     * @return 是否设置成功
     */
    public boolean setStrategy(String strategyName) {
        return strategyFactory.setCurrentStrategy(strategyName);
    }

    /**
     * 获取当前策略名称
     */
    public String getCurrentStrategyEnum() {
        MusicApiStrategy currentStrategy = strategyFactory.getCurrentStrategy();
        return currentStrategy != null ? currentStrategy.getStrategyName() : MusicStrategyEnum.NETEASE.getPlatform();
    }

    /**
     * 获取所有可用的策略名称
     */
    public String[] getAvailableStrategies() {
        return strategyFactory.getAvailableStrategies();
    }
}