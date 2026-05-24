package com.musicplayer.ui.controllers;

import com.musicplayer.models.Song;
import com.musicplayer.services.SongService;
import com.musicplayer.services.MusicPlayerService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.io.File;
import java.util.List;

/**
 * Controller for displaying song grid
 * Implements Apple Music-style grid layout with album covers
 */
public class SongGridController {
    private final SongService songService;
    private final MusicPlayerService playerService;
    private GridPane gridPane;
    private ScrollPane scrollPane;

    public SongGridController(SongService songService, MusicPlayerService playerService) {
        this.songService = songService;
        this.playerService = playerService;
        initializeUI();
    }

    /**
     * Initialize the grid layout
     */
    private void initializeUI() {
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-control-inner-background: transparent;");

        gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPadding(new Insets(20));
        gridPane.setStyle("-fx-background-color: #ffffff;");

        // Set column constraints for responsive grid
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            gridPane.getColumnConstraints().add(col);
        }

        scrollPane.setContent(gridPane);
        refreshGrid();
    }

    /**
     * Refresh the grid with songs from service
     */
    public void refreshGrid() {
        gridPane.getChildren().clear();
        List<Song> songs = songService.getAllSongs();

        int row = 0;
        int col = 0;
        for (Song song : songs) {
            VBox songCard = createSongCard(song);
            gridPane.add(songCard, col, row);

            col++;
            if (col >= 4) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Create a song card with album art and info
     */
    private VBox createSongCard(Song song) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(0));
        card.setStyle(
            "-fx-border-radius: 12; " +
            "-fx-background-color: #f9f9fb; " +
            "-fx-cursor: hand;"
        );

        // Album cover image with rounded corners
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(180);
        coverImage.setFitHeight(180);
        coverImage.setPreserveRatio(true);
        coverImage.setStyle("-fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");

        // Load cover image if exists
        if (song.getCoverPath() != null && !song.getCoverPath().isEmpty()) {
            File coverFile = new File(song.getCoverPath());
            if (coverFile.exists()) {
                try {
                    coverImage.setImage(new Image(coverFile.toURI().toString()));
                } catch (Exception e) {
                    setDefaultCover(coverImage);
                }
            } else {
                setDefaultCover(coverImage);
            }
        } else {
            setDefaultCover(coverImage);
        }

        // Song title
        Label titleLabel = new Label(song.getTitle());
        titleLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #000; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(180);

        // Artist name
        Label artistLabel = new Label(song.getArtist());
        artistLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999; -fx-wrap-text: true;");
        artistLabel.setMaxWidth(180);

        card.getChildren().addAll(coverImage, titleLabel, artistLabel);
        card.setOnMouseClicked(e -> {
            List<Song> allSongs = songService.getAllSongs();
            playerService.initializeQueue(allSongs.stream().map(Song::getId).toList());
            int index = allSongs.indexOf(song);
            playerService.play();
        });

        return card;
    }

    /**
     * Set default cover image placeholder
     */
    private void setDefaultCover(ImageView imageView) {
        imageView.setStyle("-fx-border-radius: 8; -fx-background-color: #e5e5ea;");
        Label placeholder = new Label("🎵");
        placeholder.setStyle("-fx-font-size: 60; -fx-text-alignment: center;");
    }

    /**
     * Get the grid pane component
     */
    public ScrollPane getGridPane() {
        return scrollPane;
    }
}
