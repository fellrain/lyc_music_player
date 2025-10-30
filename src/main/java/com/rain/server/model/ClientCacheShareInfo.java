package com.rain.server.model;

/**
 * 客户端缓存分享信息实体
 * @param musicId 音轨id
 * @param musicTitle 音轨名称
 */
public record ClientCacheShareInfo(String musicId,String musicTitle) {

}