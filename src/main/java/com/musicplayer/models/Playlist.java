package com.musicplayer.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a Playlist
 * Implements Serializable for file-based persistence
 */
public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private List<String> songIds; // Store song IDs instead of Song objects
    private LocalDateTime dateCreated;
    private LocalDateTime dateModified;
    private String coverPath;

    public Playlist(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.songIds = new ArrayList<>();
        this.dateCreated = LocalDateTime.now();
        this.dateModified = LocalDateTime.now();
    }

    public Playlist(String id, String name) {
        this(id, name, "");
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.dateModified = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.dateModified = LocalDateTime.now();
    }

    public List<String> getSongIds() { return new ArrayList<>(songIds); }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public LocalDateTime getDateModified() { return dateModified; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    // Song management methods
    public void addSong(String songId) {
        if (!songIds.contains(songId)) {
            songIds.add(songId);
            this.dateModified = LocalDateTime.now();
        }
    }

    public void removeSong(String songId) {
        if (songIds.remove(songId)) {
            this.dateModified = LocalDateTime.now();
        }
    }

    public boolean containsSong(String songId) {
        return songIds.contains(songId);
    }

    public int getSongCount() {
        return songIds.size();
    }

    public void clearSongs() {
        songIds.clear();
        this.dateModified = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", songCount=" + songIds.size() +
                ", dateCreated=" + dateCreated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return id.equals(playlist.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
