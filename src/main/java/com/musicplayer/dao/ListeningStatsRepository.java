package com.musicplayer.dao;

import com.musicplayer.models.ListeningStats;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Data Access Object (DAO) for ListeningStats persistence
 * Tracks user listening behavior for analytics and copyright royalty calculations
 */
public class ListeningStatsRepository {
    private final String filePath;

    public ListeningStatsRepository(String filePath) {
        this.filePath = filePath;
        ensureFileExists();
    }

    /**
     * Ensure the data file exists, create if not
     */
    private void ensureFileExists() {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating listening stats data file: " + e.getMessage());
            }
        }
    }

    /**
     * Save all stats to file
     */
    public void saveStats(List<ListeningStats> statsList) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(statsList);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving listening stats: " + e.getMessage());
        }
    }

    /**
     * Load all stats from file
     */
    public List<ListeningStats> loadStats() {
        List<ListeningStats> statsList = new ArrayList<>();
        
        if (new File(filePath).length() == 0) {
            return statsList;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            statsList = (List<ListeningStats>) ois.readObject();
        } catch (EOFException e) {
            System.out.println("Listening stats file is empty or corrupted, starting fresh");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading listening stats: " + e.getMessage());
        }

        return statsList;
    }

    /**
     * Record a listening event
     */
    public void recordListening(String songId, String userId, long listeningSeconds) {
        List<ListeningStats> statsList = loadStats();
        LocalDate today = LocalDate.now();
        
        String key = songId + "_" + userId + "_" + today;
        ListeningStats stats = statsList.stream()
                .filter(s -> s.getStatsKey().equals(key))
                .findFirst()
                .orElse(null);

        if (stats == null) {
            stats = new ListeningStats(songId, userId, today);
            statsList.add(stats);
        }

        stats.incrementListenCount();
        stats.addListeningSeconds(listeningSeconds);
        saveStats(statsList);
    }

    /**
     * Get listening stats for a specific song
     */
    public List<ListeningStats> getStatsBySong(String songId) {
        List<ListeningStats> statsList = loadStats();
        return statsList.stream()
                .filter(s -> s.getSongId().equals(songId))
                .toList();
    }

    /**
     * Get listening stats for a specific user
     */
    public List<ListeningStats> getStatsByUser(String userId) {
        List<ListeningStats> statsList = loadStats();
        return statsList.stream()
                .filter(s -> s.getUserId().equals(userId))
                .toList();
    }

    /**
     * Get total play count for a song
     */
    public int getTotalPlayCount(String songId) {
        return getStatsBySong(songId).stream()
                .mapToInt(ListeningStats::getListenCount)
                .sum();
    }

    /**
     * Get total listening time for a user (in seconds)
     */
    public long getUserTotalListeningTime(String userId) {
        return getStatsByUser(userId).stream()
                .mapToLong(ListeningStats::getTotalListeningSeconds)
                .sum();
    }

    /**
     * Get stats for a date range
     */
    public List<ListeningStats> getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<ListeningStats> statsList = loadStats();
        return statsList.stream()
                .filter(s -> !s.getListenDate().isBefore(startDate) && 
                           !s.getListenDate().isAfter(endDate))
                .toList();
    }

    /**
     * Get top songs by play count
     */
    public List<Map.Entry<String, Integer>> getTopSongsByPlayCount(int limit) {
        Map<String, Integer> songPlayCount = new HashMap<>();
        List<ListeningStats> statsList = loadStats();
        
        for (ListeningStats stats : statsList) {
            songPlayCount.merge(stats.getSongId(), stats.getListenCount(), Integer::sum);
        }

        return songPlayCount.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .toList();
    }

    /**
     * Calculate copyright royalty for a song (simplified: $0.004 per play)
     */
    public double calculateRoyalty(String songId, double ratePerPlay) {
        int totalPlays = getTotalPlayCount(songId);
        return totalPlays * ratePerPlay;
    }

    /**
     * Clear all stats
     */
    public void clearAll() {
        saveStats(new ArrayList<>());
    }

    /**
     * Get total number of stats records
     */
    public int getStatsCount() {
        return loadStats().size();
    }
}
