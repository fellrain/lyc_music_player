package com.rain.model;

/**
 * 音乐轨道实体
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public class MusicTrack {

    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long SECONDS_PER_MINUTE = 60;

    private final String id;
    private final String title;
    private final String artist;
    private final String album;
    private String url;
    private final long duration;

    public MusicTrack(String id, String title, String artist, String album,
                      String url, long duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.url = url;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getUrl() {
        return url;
    }

    public long getDuration() {
        return duration;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFormattedDuration() {
        long totalSeconds = duration / MILLISECONDS_PER_SECOND,
                minutes = totalSeconds / SECONDS_PER_MINUTE,
                seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", artist, title, getFormattedDuration());
    }
}
