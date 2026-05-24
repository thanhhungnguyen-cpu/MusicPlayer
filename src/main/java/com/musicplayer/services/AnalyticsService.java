package com.musicplayer.services;

import com.musicplayer.dao.ListeningStatsRepository;
import com.musicplayer.dao.SongRepository;
import com.musicplayer.models.ListeningStats;
import com.musicplayer.models.Song;

import java.time.LocalDate;
import java.util.*;

/**
 * Service class for analytics and royalty calculations
 * Tracks listening patterns and calculates copyright royalties
 */
public class AnalyticsService {
    private final ListeningStatsRepository statsRepository;
    private final SongRepository songRepository;

    public static final double DEFAULT_ROYALTY_RATE = 0.004; // $0.004 per stream

    public AnalyticsService(ListeningStatsRepository statsRepository, SongRepository songRepository) {
        this.statsRepository = statsRepository;
        this.songRepository = songRepository;
    }

    /**
     * Record a listening event
     */
    public void recordListening(String songId, String userId, long listeningSeconds) {
        statsRepository.recordListening(songId, userId, listeningSeconds);
        // Update song play count
        Song song = songRepository.getSongById(songId);
        if (song != null) {
            song.incrementPlayCount();
            songRepository.updateSong(song);
        }
    }

    /**
     * Get total play count for a song
     */
    public int getTotalPlayCount(String songId) {
        return statsRepository.getTotalPlayCount(songId);
    }

    /**
     * Get user total listening time (in hours)
     */
    public double getUserTotalListeningTime(String userId) {
        long seconds = statsRepository.getUserTotalListeningTime(userId);
        return seconds / 3600.0;
    }

    /**
     * Get top songs by play count
     */
    public List<Map.Entry<String, Integer>> getTopSongs(int limit) {
        return statsRepository.getTopSongsByPlayCount(limit);
    }

    /**
     * Get top songs with full details
     */
    public List<Map.Entry<Song, Integer>> getTopSongsWithDetails(int limit) {
        List<Map.Entry<String, Integer>> topSongIds = statsRepository.getTopSongsByPlayCount(limit);
        List<Map.Entry<Song, Integer>> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : topSongIds) {
            Song song = songRepository.getSongById(entry.getKey());
            if (song != null) {
                result.add(new AbstractMap.SimpleEntry<>(song, entry.getValue()));
            }
        }

        return result;
    }

    /**
     * Calculate copyright royalty for a song
     */
    public double calculateRoyalty(String songId) {
        return calculateRoyalty(songId, DEFAULT_ROYALTY_RATE);
    }

    /**
     * Calculate copyright royalty with custom rate
     */
    public double calculateRoyalty(String songId, double ratePerPlay) {
        return statsRepository.calculateRoyalty(songId, ratePerPlay);
    }

    /**
     * Get listening stats for a date range
     */
    public List<ListeningStats> getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        return statsRepository.getStatsByDateRange(startDate, endDate);
    }

    /**
     * Get daily active users count
     */
    public int getDailyActiveUsers(LocalDate date) {
        return (int) statsRepository.getStatsByDateRange(date, date).stream()
                .map(ListeningStats::getUserId)
                .distinct()
                .count();
    }

    /**
     * Get monthly statistics summary
     */
    public Map<String, Object> getMonthlyStats(LocalDate startDate) {
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        List<ListeningStats> stats = statsRepository.getStatsByDateRange(startDate, endDate);

        Map<String, Object> summary = new HashMap<>();
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("totalStreams", stats.size());
        summary.put("uniqueUsers", stats.stream()
                .map(ListeningStats::getUserId)
                .distinct()
                .count());
        summary.put("totalListeningSeconds", stats.stream()
                .mapToLong(ListeningStats::getTotalListeningSeconds)
                .sum());

        return summary;
    }
}
