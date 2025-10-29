package com.rain.client.network.strategy;

import com.rain.client.model.Lyric;
import com.rain.common.util.CollUtil;
import com.rain.common.util.StrUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 落雨川
 */
public abstract class AbstractMusicApiStrategy implements MusicApiStrategy {

    private static final Pattern LRC_LINE_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})](.*)");

    private static final int MINUTES_TO_MS = 60000;

    private static final int SECONDS_TO_MS = 1000;

    private static final int MS_SCALE = 10;

    /**
     * 解析LRC格式歌词
     */
    public List<Lyric.LyricLine> parseLrc(Map<String, Object> context) {
        if (CollUtil.isEmpty(context)) {
            return Collections.emptyList();
        }
        String content = (String) context.get("content");
        if (StrUtil.isEmpty(content)) {
            return Collections.emptyList();
        }
        List<Lyric.LyricLine> result = new ArrayList<>();
        String[] lines = content.split("\n");
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
                        result.add(new Lyric.LyricLine(timestamp, text));
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式错误的行
                }
            }
        }
        Collections.sort(result);
        return result;
    }
}
