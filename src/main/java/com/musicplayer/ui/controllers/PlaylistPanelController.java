package com.musicplayer.ui.controllers;

import com.musicplayer.models.Playlist;
import com.musicplayer.models.Song;
import com.musicplayer.services.PlaylistService;
import com.musicplayer.services.MusicPlayerService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * Controller for playlist panel
 * Displays and manages user playlists
 */
public class PlaylistPanelController {
    private final PlaylistService playlistService;
    private final MusicPlayerService playerService;
    private VBox playlistList;
    private ListView<Playlist> listView;

    public PlaylistPanelController(PlaylistService playlistService, MusicPlayerService playerService) {
        this.playlistService = playlistService;
        this.playerService = playerService;
        initializeUI();
    }

    /**
     * Initialize the playlist panel UI
     */
    private void initializeUI() {
        playlistList = new VBox(10);
        playlistList.setPadding(new Insets(10, 0, 0, 0));
        playlistList.setStyle("-fx-background-color: transparent;");

        // Playlist ListView
        listView = new ListView<>();
        listView.setStyle(
            "-fx-control-inner-background: transparent; " +
            "-fx-padding: 0; " +
            "-fx-border-color: transparent;"
        );
        listView.setCellFactory(param -> new PlaylistCell());

        refreshPlaylists();

        playlistList.getChildren().addAll(listView);
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);
    }

    /**
     * Refresh playlist list
     */
    public void refreshPlaylists() {
        List<Playlist> playlists = playlistService.getAllPlaylists();
        listView.getItems().setAll(playlists);
    }

    /**
     * Custom cell for displaying playlists
     */
    private class PlaylistCell extends ListCell<Playlist> {
        @Override
        protected void updateItem(Playlist item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                Label playlistLabel = new Label(item.getName());
                playlistLabel.setStyle(
                    "-fx-font-size: 12; " +
                    "-fx-text-fill: #555; " +
                    "-fx-padding: 8 10;"
                );
                setGraphic(playlistLabel);
                setStyle("-fx-padding: 0; -fx-background-color: transparent;");

                setOnMouseClicked(e -> {
                    playerService.loadPlaylist(item.getId());
                    playerService.play();
                });
            }
        }
    }

    /**
     * Get the playlist list component
     */
    public VBox getPlaylistList() {
        return playlistList;
    }
}
