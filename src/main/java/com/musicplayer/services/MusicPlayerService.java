package com.musicplayer.services;

import com.musicplayer.models.Song;
import com.musicplayer.models.Playlist;
import com.musicplayer.dao.SongRepository;
import com.musicplayer.dao.PlaylistRepository;
import com.musicplayer.dao.ListeningStatsRepository;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service class for managing music playback
 * Implements advanced playback features: shuffle, loop, queue management
 * Uses JavaFX MediaPlayer for audio playback
 */
public class MusicPlayerService {
    public enum PlayMode {
        SEQUENTIAL, LOOP_ONE, LOOP_ALL, SHUFFLE
    }

    private MediaPlayer mediaPlayer;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final ListeningStatsRepository statsRepository;

    private List<String> currentQueue; // Song IDs in queue
    private int currentIndex;
    private PlayMode playMode;
    private boolean isPaused;
    private long currentPlayingSeconds; // For stats tracking
    private String currentUserId;

    private final List<PlaybackListener> playbackListeners;

    public interface PlaybackListener {
        void onPlaybackStatusChanged(boolean isPlaying);
        void onCurrentSongChanged(Song song);
        void onProgressChanged(double currentTime, double totalTime);
        void onQueueChanged();
    }

    public MusicPlayerService(SongRepository songRepository, 
                              PlaylistRepository playlistRepository,
                              ListeningStatsRepository statsRepository) {
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
        this.statsRepository = statsRepository;
        this.currentQueue = new ArrayList<>();
        this.currentIndex = 0;
        this.playMode = PlayMode.SEQUENTIAL;
        this.isPaused = true;
        this.currentPlayingSeconds = 0;
        this.playbackListeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Initialize player with a song list
     */
    public void initializeQueue(List<String> songIds) {
        this.currentQueue = new ArrayList<>(songIds);
        this.currentIndex = 0;
        notifyQueueChanged();
    }

    /**
     * Load and play a specific song
     */
    public void playSong(String songId) {
        Song song = songRepository.getSongById(songId);
        if (song == null) {
            System.err.println("Song not found: " + songId);
            return;
        }

        // Stop previous playback
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        // Create and play new media
        try {
            File mediaFile = new File(song.getFilePath());
            if (!mediaFile.exists()) {
                System.err.println("Audio file not found: " + song.getFilePath());
                return;
            }

            Media media = new Media(mediaFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            // Setup event listeners
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                isPaused = false;
                currentPlayingSeconds = 0;
                notifyPlaybackStatusChanged(true);
                notifyCurrentSongChanged(song);
            });

            mediaPlayer.setOnEndOfMedia(this::playNext);

            // Progress update
            mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
                if (mediaPlayer != null && !mediaPlayer.isMute()) {
                    currentPlayingSeconds = (long) newVal.toSeconds();
                    notifyProgressChanged(newVal.toSeconds(), mediaPlayer.getTotalDuration().toSeconds());
                }
            });
        } catch (Exception e) {
            System.err.println("Error playing song: " + e.getMessage());
        }
    }

    /**
     * Play the song at current queue index
     */
    public void play() {
        if (currentQueue.isEmpty()) {
            System.err.println("Queue is empty");
            return;
        }

        if (mediaPlayer != null && isPaused) {
            mediaPlayer.play();
            isPaused = false;
            notifyPlaybackStatusChanged(true);
            return;
        }

        String songId = currentQueue.get(currentIndex);
        playSong(songId);
    }

    /**
     * Pause playback
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            isPaused = true;
            notifyPlaybackStatusChanged(false);
        }
    }

    /**
     * Stop playback and reset
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        isPaused = true;
        currentIndex = 0;
        currentPlayingSeconds = 0;
        notifyPlaybackStatusChanged(false);
    }

    /**
     * Play next song based on play mode
     */
    public void playNext() {
        if (currentQueue.isEmpty()) return;

        switch (playMode) {
            case SHUFFLE:
                currentIndex = new Random().nextInt(currentQueue.size());
                break;
            case LOOP_ONE:
                // Replay current song
                break;
            case LOOP_ALL:
                currentIndex = (currentIndex + 1) % currentQueue.size();
                break;
            case SEQUENTIAL:
            default:
                currentIndex++;
                if (currentIndex >= currentQueue.size()) {
                    currentIndex = currentQueue.size() - 1;
                    stop();
                    return;
                }
        }

        String nextSongId = currentQueue.get(currentIndex);
        playSong(nextSongId);
    }

    /**
     * Play previous song
     */
    public void playPrevious() {
        if (currentQueue.isEmpty()) return;

        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = 0;
        }

        String prevSongId = currentQueue.get(currentIndex);
        playSong(prevSongId);
    }

    /**
     * Seek to a specific time in the current song
     */
    public void seek(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(seconds));
        }
    }

    /**
     * Set volume (0.0 to 1.0)
     */
    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(Math.max(0, Math.min(1, volume)));
        }
    }

    /**
     * Get current volume
     */
    public double getVolume() {
        return mediaPlayer != null ? mediaPlayer.getVolume() : 0.5;
    }

    /**
     * Set play mode
     */
    public void setPlayMode(PlayMode mode) {
        this.playMode = mode;
    }

    /**
     * Get current play mode
     */
    public PlayMode getPlayMode() {
        return playMode;
    }

    /**
     * Get current song
     */
    public Song getCurrentSong() {
        if (currentQueue.isEmpty() || currentIndex >= currentQueue.size()) {
            return null;
        }
        return songRepository.getSongById(currentQueue.get(currentIndex));
    }

    /**
     * Get current playback time in seconds
     */
    public double getCurrentTime() {
        return mediaPlayer != null ? mediaPlayer.getCurrentTime().toSeconds() : 0;
    }

    /**
     * Get total duration in seconds
     */
    public double getTotalDuration() {
        return mediaPlayer != null ? mediaPlayer.getTotalDuration().toSeconds() : 0;
    }

    /**
     * Check if currently playing
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    /**
     * Load playlist into queue
     */
    public void loadPlaylist(String playlistId) {
        Playlist playlist = playlistRepository.getPlaylistById(playlistId);
        if (playlist != null) {
            initializeQueue(playlist.getSongIds());
        }
    }

    /**
     * Get current queue
     */
    public List<Song> getQueue() {
        List<Song> songs = new ArrayList<>();
        for (String songId : currentQueue) {
            Song song = songRepository.getSongById(songId);
            if (song != null) {
                songs.add(song);
            }
        }
        return songs;
    }

    /**
     * Add listener for playback events
     */
    public void addPlaybackListener(PlaybackListener listener) {
        playbackListeners.add(listener);
    }

    /**
     * Remove listener
     */
    public void removePlaybackListener(PlaybackListener listener) {
        playbackListeners.remove(listener);
    }

    // Notification methods
    private void notifyPlaybackStatusChanged(boolean isPlaying) {
        for (PlaybackListener listener : playbackListeners) {
            listener.onPlaybackStatusChanged(isPlaying);
        }
    }

    private void notifyCurrentSongChanged(Song song) {
        for (PlaybackListener listener : playbackListeners) {
            listener.onCurrentSongChanged(song);
        }
    }

    private void notifyProgressChanged(double currentTime, double totalTime) {
        for (PlaybackListener listener : playbackListeners) {
            listener.onProgressChanged(currentTime, totalTime);
        }
    }

    private void notifyQueueChanged() {
        for (PlaybackListener listener : playbackListeners) {
            listener.onQueueChanged();
        }
    }

    /**
     * Release resources
     */
    public void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }
}
