package com.rain.client.network.strategy;

import com.rain.client.MusicPlayerClientMod;
import com.rain.common.enums.MusicStrategyEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 音乐API策略工厂
 *
 * @author 落雨川
 * @version 1.7
 * @since 1.7
 */
public class MusicApiStrategyFactory {

    private static MusicApiStrategyFactory instance;

    private final Map<String, MusicApiStrategy> strategies = new HashMap<>(16);

    private MusicApiStrategy currentStrategy;

    private MusicApiStrategyFactory() {
        loadStrategies();
    }

    /**
     * 获取工厂单例实例
     */
    public static synchronized MusicApiStrategyFactory getInstance() {
        if (instance == null) {
            instance = new MusicApiStrategyFactory();
        }
        return instance;
    }

    /**
     * 加载所有可用的策略实现(SPI)
     */
    private void loadStrategies() {
        ServiceLoader<MusicApiStrategy> loader = ServiceLoader.load(MusicApiStrategy.class);
        for (MusicApiStrategy strategy : loader) {
            strategies.put(strategy.getStrategyName(), strategy);
            MusicPlayerClientMod.LOGGER.info("加载音乐API策略: {}", strategy.getStrategyName());
        }
        // 设置默认WWY策略
        setCurrentStrategy(MusicStrategyEnum.NETEASE.getPlatform());
    }

    /**
     * 设置当前使用的策略
     *
     * @param strategyName 策略名称
     * @return 是否设置成功
     */
    public boolean setCurrentStrategy(String strategyName) {
        MusicApiStrategy strategy = strategies.get(strategyName);
        if (strategy != null) {
            this.currentStrategy = strategy;
            MusicPlayerClientMod.LOGGER.info("切换音乐API策略为: {}", strategyName);
            return true;
        } else {
            MusicPlayerClientMod.LOGGER.warn("未找到音乐API策略: {}", strategyName);
            return false;
        }
    }

    /**
     * 获取当前策略
     */
    public MusicApiStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    /**
     * 获取所有可用的策略名称
     */
    public String[] getAvailableStrategies() {
        return strategies.keySet().toArray(new String[0]);
    }

    /**
     * 根据名称获取策略
     */
    public MusicApiStrategy getStrategy(String name) {
        return strategies.get(name);
    }
}