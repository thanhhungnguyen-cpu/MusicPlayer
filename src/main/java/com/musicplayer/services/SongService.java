package com.musicplayer.services;

import com.musicplayer.models.Song;
import com.musicplayer.dao.SongRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service class for managing songs
 * Provides business logic for song operations
 */
public class SongService {
    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    /**
     * Add a new song to the library
     */
    public Song addSong(String title, String artist, String album, String genre, 
                        int duration, String filePath, String coverPath) {
        String songId = UUID.randomUUID().toString();
        Song song = new Song(songId, title, artist, album, genre, duration, filePath, coverPath);
        songRepository.addSong(song);
        return song;
    }

    /**
     * Get all songs
     */
    public List<Song> getAllSongs() {
        return songRepository.getAllSongs();
    }

    /**
     * Get song by ID
     */
    public Song getSong(String songId) {
        return songRepository.getSongById(songId);
    }

    /**
     * Update song info
     */
    public void updateSong(Song song) {
        songRepository.updateSong(song);
    }

    /**
     * Delete a song
     */
    public void deleteSong(String songId) {
        songRepository.deleteSong(songId);
    }

    /**
     * Get songs by artist
     */
    public List<Song> getSongsByArtist(String artist) {
        return songRepository.getSongsByArtist(artist);
    }

    /**
     * Get songs by album
     */
    public List<Song> getSongsByAlbum(String album) {
        return songRepository.getSongsByAlbum(album);
    }

    /**
     * Get songs by genre
     */
    public List<Song> getSongsByGenre(String genre) {
        return songRepository.getSongsByGenre(genre);
    }

    /**
     * Get top songs by play count
     */
    public List<Song> getTopSongs(int limit) {
        return songRepository.getTopSongs(limit);
    }

    /**
     * Increment play count for a song
     */
    public void incrementPlayCount(String songId) {
        Song song = songRepository.getSongById(songId);
        if (song != null) {
            song.incrementPlayCount();
            songRepository.updateSong(song);
        }
    }
}
