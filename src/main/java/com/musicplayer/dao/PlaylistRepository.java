package com.musicplayer.dao;

import com.musicplayer.models.Playlist;

import java.io.*;
import java.util.*;

/**
 * Data Access Object (DAO) for Playlist persistence
 * Handles serialization/deserialization of Playlist objects to/from files
 */
public class PlaylistRepository {
    private final String filePath;

    public PlaylistRepository(String filePath) {
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
                System.err.println("Error creating playlist data file: " + e.getMessage());
            }
        }
    }

    /**
     * Save all playlists to file
     */
    public void savePlaylists(List<Playlist> playlists) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(playlists);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving playlists: " + e.getMessage());
        }
    }

    /**
     * Load all playlists from file
     */
    public List<Playlist> loadPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        
        if (new File(filePath).length() == 0) {
            return playlists;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            playlists = (List<Playlist>) ois.readObject();
        } catch (EOFException e) {
            System.out.println("Playlist data file is empty or corrupted, starting fresh");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading playlists: " + e.getMessage());
        }

        return playlists;
    }

    /**
     * Create a new playlist
     */
    public void createPlaylist(Playlist playlist) {
        List<Playlist> playlists = loadPlaylists();
        if (!playlists.contains(playlist)) {
            playlists.add(playlist);
            savePlaylists(playlists);
        }
    }

    /**
     * Update an existing playlist
     */
    public void updatePlaylist(Playlist playlist) {
        List<Playlist> playlists = loadPlaylists();
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getId().equals(playlist.getId())) {
                playlists.set(i, playlist);
                savePlaylists(playlists);
                return;
            }
        }
    }

    /**
     * Delete a playlist by ID
     */
    public void deletePlaylist(String playlistId) {
        List<Playlist> playlists = loadPlaylists();
        playlists.removeIf(p -> p.getId().equals(playlistId));
        savePlaylists(playlists);
    }

    /**
     * Get a playlist by ID
     */
    public Playlist getPlaylistById(String playlistId) {
        List<Playlist> playlists = loadPlaylists();
        return playlists.stream()
                .filter(p -> p.getId().equals(playlistId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all playlists
     */
    public List<Playlist> getAllPlaylists() {
        return loadPlaylists();
    }

    /**
     * Add song to playlist
     */
    public void addSongToPlaylist(String playlistId, String songId) {
        Playlist playlist = getPlaylistById(playlistId);
        if (playlist != null) {
            playlist.addSong(songId);
            updatePlaylist(playlist);
        }
    }

    /**
     * Remove song from playlist
     */
    public void removeSongFromPlaylist(String playlistId, String songId) {
        Playlist playlist = getPlaylistById(playlistId);
        if (playlist != null) {
            playlist.removeSong(songId);
            updatePlaylist(playlist);
        }
    }

    /**
     * Get playlists by name (partial match)
     */
    public List<Playlist> searchPlaylistsByName(String name) {
        List<Playlist> playlists = loadPlaylists();
        return playlists.stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Clear all playlists
     */
    public void clearAll() {
        savePlaylists(new ArrayList<>());
    }

    /**
     * Get total number of playlists
     */
    public int getPlaylistCount() {
        return loadPlaylists().size();
    }
}
