package com.rain.client.model;

import com.rain.common.util.CollUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词模型
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class Lyric {

    private static final Pattern LRC_LINE_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})](.*)");
    private static final int MINUTES_TO_MS = 60000;
    private static final int SECONDS_TO_MS = 1000;
    private static final int MS_SCALE = 10;

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

    public Lyric(String lrcContent, String translationContent) {
        this.lines = parseLrc(lrcContent);
        this.translationLines = parseLrc(translationContent);
    }

    /**
     * 解析LRC格式歌词
     */
    private List<LyricLine> parseLrc(String lrcContent) {
        if (lrcContent == null || lrcContent.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<LyricLine> result = new ArrayList<>();
        String[] lines = lrcContent.split("\n");
        for (String line : lines) {
            Matcher matcher = LRC_LINE_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                try {
                    int minutes = Integer.parseInt(matcher.group(1));
                    int seconds = Integer.parseInt(matcher.group(2));
                    String msStr = matcher.group(3);
                    // 处理2位或3位毫秒
                    int milliseconds = msStr.length() == 2 ? 
                        Integer.parseInt(msStr) * MS_SCALE : 
                        Integer.parseInt(msStr);
                    String text = matcher.group(4).trim();
                    // 跳过空行和元数据
                    if (!text.isEmpty() && !text.startsWith("{")) {
                        long timestamp = minutes * MINUTES_TO_MS + 
                                       seconds * SECONDS_TO_MS + 
                                       milliseconds;
                        result.add(new LyricLine(timestamp, text));
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式错误的行
                }
            }
        }
        Collections.sort(result);
        return result;
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
