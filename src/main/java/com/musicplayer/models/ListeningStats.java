package com.musicplayer.models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Model class for tracking listening statistics
 * Used for analytics and copyright royalty calculations
 */
public class ListeningStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private String songId;
    private String userId;
    private LocalDate listenDate;
    private int listenCount;
    private long totalListeningSeconds;

    public ListeningStats(String songId, String userId, LocalDate listenDate) {
        this.songId = songId;
        this.userId = userId;
        this.listenDate = listenDate;
        this.listenCount = 0;
        this.totalListeningSeconds = 0;
    }

    // Getters and Setters
    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getListenDate() { return listenDate; }
    public void setListenDate(LocalDate listenDate) { this.listenDate = listenDate; }

    public int getListenCount() { return listenCount; }
    public void setListenCount(int listenCount) { this.listenCount = listenCount; }
    public void incrementListenCount() { this.listenCount++; }

    public long getTotalListeningSeconds() { return totalListeningSeconds; }
    public void setTotalListeningSeconds(long seconds) { this.totalListeningSeconds = seconds; }
    public void addListeningSeconds(long seconds) { this.totalListeningSeconds += seconds; }

    public String getStatsKey() {
        return songId + "_" + userId + "_" + listenDate;
    }

    @Override
    public String toString() {
        return "ListeningStats{" +
                "songId='" + songId + '\'' +
                ", userId='" + userId + '\'' +
                ", listenDate=" + listenDate +
                ", listenCount=" + listenCount +
                '}';
    }
}
