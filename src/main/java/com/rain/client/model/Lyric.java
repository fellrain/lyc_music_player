package com.rain.client.model;

import com.rain.common.util.CollUtil;

import java.util.Collections;
import java.util.List;

/**
 * 歌词模型
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class Lyric {

    private final List<LyricLine> lines;

    private final List<LyricLine> translationLines;

    /**
     * 歌词行
     */
    public static class LyricLine implements Comparable<LyricLine> {
        private final long timestamp;
        private final String text;

        public LyricLine(long timestamp, String text) {
            this.timestamp = timestamp;
            this.text = text;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getText() {
            return text;
        }

        @Override
        public int compareTo(LyricLine other) {
            return Long.compare(this.timestamp, other.timestamp);
        }
    }

    public Lyric(List<LyricLine> lines, List<LyricLine> translationLines) {
        this.lines = lines;
        this.translationLines = translationLines;
    }

    /**
     * 根据当前播放时间获取歌词
     *
     * @param currentPosition 当前播放位置（毫秒）
     * @return 当前歌词，如果没有返回null
     */
    public LyricLine getCurrentLine(long currentPosition) {
        if (CollUtil.isEmpty(lines)) {
            return null;
        }
        LyricLine current = null;
        for (LyricLine line : lines) {
            if (line.getTimestamp() <= currentPosition) {
                current = line;
            } else {
                break;
            }
        }
        return current;
    }

    /**
     * 根据当前播放时间获取翻译歌词
     */
    public LyricLine getCurrentTranslation(long currentPosition) {
        if (translationLines.isEmpty()) {
            return null;
        }

        LyricLine current = null;
        for (LyricLine line : translationLines) {
            if (line.getTimestamp() <= currentPosition) {
                current = line;
            } else {
                break;
            }
        }
        return current;
    }

    /**
     * 获取下一句歌词
     */
    public LyricLine getNextLine(long currentPosition) {
        for (LyricLine line : lines) {
            if (line.getTimestamp() > currentPosition) {
                return line;
            }
        }
        return null;
    }

    /**
     * 判断是否有歌词
     */
    public boolean hasLyric() {
        return !CollUtil.isEmpty(lines);
    }

    /**
     * 获取所有歌词行
     */
    public List<LyricLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * 获取所有翻译行
     */
    public List<LyricLine> getTranslationLines() {
        return Collections.unmodifiableList(translationLines);
    }
}
