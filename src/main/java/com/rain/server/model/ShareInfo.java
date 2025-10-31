package com.rain.server.model;

/**
 * 分享信息记录
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class ShareInfo {

    String shareId;
    String senderUuid;
    String senderName;
    String musicId;
    String musicTitle;
    String musicArtist;
    String targetUser;
    long timestamp;

    public ShareInfo(String shareId, String senderUuid, String senderName, String musicId, String musicTitle, String musicArtist, String targetUser, long timestamp) {
        this.shareId = shareId;
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.musicId = musicId;
        this.musicTitle = musicTitle;
        this.musicArtist = musicArtist;
        this.targetUser = targetUser;
        this.timestamp = timestamp;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getSenderUuid() {
        return senderUuid;
    }

    public void setSenderUuid(String senderUuid) {
        this.senderUuid = senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
