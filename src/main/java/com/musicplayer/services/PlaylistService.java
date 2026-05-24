package com.musicplayer.services;

import com.musicplayer.models.Playlist;
import com.musicplayer.models.Song;
import com.musicplayer.dao.PlaylistRepository;
import com.musicplayer.dao.SongRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service class for managing playlists
 * Provides business logic for playlist operations
 */
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public PlaylistService(PlaylistRepository playlistRepository, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
    }

    /**
     * Create a new playlist
     */
    public Playlist createPlaylist(String name, String description) {
        String playlistId = UUID.randomUUID().toString();
        Playlist playlist = new Playlist(playlistId, name, description);
        playlistRepository.createPlaylist(playlist);
        return playlist;
    }

    /**
     * Delete a playlist
     */
    public void deletePlaylist(String playlistId) {
        playlistRepository.deletePlaylist(playlistId);
    }

    /**
     * Add a song to a playlist
     */
    public void addSongToPlaylist(String playlistId, String songId) {
        playlistRepository.addSongToPlaylist(playlistId, songId);
    }

    /**
     * Remove a song from a playlist
     */
    public void removeSongFromPlaylist(String playlistId, String songId) {
        playlistRepository.removeSongFromPlaylist(playlistId, songId);
    }

    /**
     * Get all songs in a playlist
     */
    public List<Song> getPlaylistSongs(String playlistId) {
        Playlist playlist = playlistRepository.getPlaylistById(playlistId);
        if (playlist == null) return List.of();
        
        return playlist.getSongIds().stream()
                .map(songRepository::getSongById)
                .filter(song -> song != null)
                .toList();
    }

    /**
     * Get all playlists
     */
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.getAllPlaylists();
    }

    /**
     * Get playlist by ID
     */
    public Playlist getPlaylist(String playlistId) {
        return playlistRepository.getPlaylistById(playlistId);
    }

    /**
     * Update playlist
     */
    public void updatePlaylist(Playlist playlist) {
        playlistRepository.updatePlaylist(playlist);
    }

    /**
     * Search playlists by name
     */
    public List<Playlist> searchPlaylistsByName(String name) {
        return playlistRepository.searchPlaylistsByName(name);
    }
}
