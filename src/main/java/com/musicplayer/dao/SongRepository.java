package com.musicplayer.dao;

import com.musicplayer.models.Song;

import java.io.*;
import java.util.*;

/**
 * Data Access Object (DAO) for Song persistence
 * Handles serialization/deserialization of Song objects to/from files
 * Uses ObjectInputStream/ObjectOutputStream for efficient binary serialization
 */
public class SongRepository {
    private final String filePath;

    public SongRepository(String filePath) {
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
                System.err.println("Error creating song data file: " + e.getMessage());
            }
        }
    }

    /**
     * Save all songs to file using ObjectOutputStream
     * Uses try-with-resources to ensure proper resource management
     */
    public void saveSongs(List<Song> songs) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(songs);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving songs: " + e.getMessage());
        }
    }

    /**
     * Load all songs from file using ObjectInputStream
     * Handles EOFException gracefully for empty files
     */
    public List<Song> loadSongs() {
        List<Song> songs = new ArrayList<>();
        
        if (new File(filePath).length() == 0) {
            return songs; // Return empty list for empty file
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            songs = (List<Song>) ois.readObject();
        } catch (EOFException e) {
            System.out.println("Song data file is empty or corrupted, starting fresh");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading songs: " + e.getMessage());
        }

        return songs;
    }

    /**
     * Add a new song and persist
     */
    public void addSong(Song song) {
        List<Song> songs = loadSongs();
        if (!songs.contains(song)) {
            songs.add(song);
            saveSongs(songs);
        }
    }

    /**
     * Update an existing song and persist
     */
    public void updateSong(Song song) {
        List<Song> songs = loadSongs();
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId().equals(song.getId())) {
                songs.set(i, song);
                saveSongs(songs);
                return;
            }
        }
    }

    /**
     * Delete a song by ID and persist
     */
    public void deleteSong(String songId) {
        List<Song> songs = loadSongs();
        songs.removeIf(s -> s.getId().equals(songId));
        saveSongs(songs);
    }

    /**
     * Get a song by ID
     */
    public Song getSongById(String songId) {
        List<Song> songs = loadSongs();
        return songs.stream()
                .filter(s -> s.getId().equals(songId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all songs
     */
    public List<Song> getAllSongs() {
        return loadSongs();
    }

    /**
     * Get songs by artist
     */
    public List<Song> getSongsByArtist(String artist) {
        List<Song> songs = loadSongs();
        return songs.stream()
                .filter(s -> s.getArtist().equalsIgnoreCase(artist))
                .toList();
    }

    /**
     * Get songs by album
     */
    public List<Song> getSongsByAlbum(String album) {
        List<Song> songs = loadSongs();
        return songs.stream()
                .filter(s -> s.getAlbum().equalsIgnoreCase(album))
                .toList();
    }

    /**
     * Get songs by genre
     */
    public List<Song> getSongsByGenre(String genre) {
        List<Song> songs = loadSongs();
        return songs.stream()
                .filter(s -> s.getGenre().equalsIgnoreCase(genre))
                .toList();
    }

    /**
     * Get top songs by play count
     */
    public List<Song> getTopSongs(int limit) {
        List<Song> songs = loadSongs();
        return songs.stream()
                .sorted((s1, s2) -> Integer.compare(s2.getPlayCount(), s1.getPlayCount()))
                .limit(limit)
                .toList();
    }

    /**
     * Clear all songs
     */
    public void clearAll() {
        saveSongs(new ArrayList<>());
    }

    /**
     * Get total number of songs
     */
    public int getSongCount() {
        return loadSongs().size();
    }
}
