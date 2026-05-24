package com.musicplayer.ui.controllers;

import com.musicplayer.models.Song;
import com.musicplayer.services.MusicPlayerService;
import com.musicplayer.services.SongService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.io.File;

/**
 * Controller for bottom player control bar
 * Apple Music-style player with playback controls and progress bar
 * Implements MusicPlayerService.PlaybackListener for real-time updates
 */
public class BottomPlayerController implements MusicPlayerService.PlaybackListener {
    private final MusicPlayerService playerService;
    private final SongService songService;
    private HBox controlBar;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Button playPauseBtn;
    private Label songTitleLabel;
    private Label artistLabel;
    private ImageView albumArtSmall;
    private ComboBox<MusicPlayerService.PlayMode> playModeCombo;

    public BottomPlayerController(MusicPlayerService playerService, SongService songService) {
        this.playerService = playerService;
        this.songService = songService;
        initializeUI();
        startProgressUpdater();
    }

    /**
     * Initialize the control bar UI
     */
    private void initializeUI() {
        controlBar = new HBox(15);
        controlBar.setPadding(new Insets(15));
        controlBar.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-top: 1px solid #e5e5ea; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, -2);"
        );
        controlBar.setAlignment(Pos.CENTER_LEFT);

        // Left side: Album art and song info
        VBox songInfoBox = createSongInfoBox();
        controlBar.getChildren().add(songInfoBox);

        // Center: Playback controls and progress
        VBox centerControls = createPlaybackControls();
        HBox.setHgrow(centerControls, Priority.ALWAYS);
        controlBar.getChildren().add(centerControls);

        // Right side: Volume and mode controls
        HBox rightControls = createRightControls();
        controlBar.getChildren().add(rightControls);
    }

    /**
     * Create song info section
     */
    private VBox createSongInfoBox() {
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: transparent;");
        infoBox.setPrefWidth(200);

        // Album art thumbnail
        albumArtSmall = new ImageView();
        albumArtSmall.setFitWidth(50);
        albumArtSmall.setFitHeight(50);
        albumArtSmall.setStyle("-fx-border-radius: 4; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");

        // Song info
        songTitleLabel = new Label("No song playing");
        songTitleLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #000;");
        songTitleLabel.setMaxWidth(150);
        songTitleLabel.setWrapText(true);

        artistLabel = new Label("Unknown Artist");
        artistLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");
        artistLabel.setMaxWidth(150);
        artistLabel.setWrapText(true);

        infoBox.getChildren().addAll(albumArtSmall, songTitleLabel, artistLabel);
        return infoBox;
    }

    /**
     * Create playback controls and progress bar
     */
    private VBox createPlaybackControls() {
        VBox controlsBox = new VBox(8);
        controlsBox.setAlignment(Pos.CENTER);

        // Playback buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button prevBtn = createControlButton("⏮", e -> playerService.playPrevious());
        playPauseBtn = createControlButton("▶", e -> togglePlayPause());
        playPauseBtn.setPrefWidth(50);
        playPauseBtn.setPrefHeight(50);
        playPauseBtn.setStyle(
            "-fx-font-size: 16; " +
            "-fx-padding: 10; " +
            "-fx-background-color: #007AFF; " +
            "-fx-text-fill: white; " +
            "-fx-border-radius: 25; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand;"
        );

        Button nextBtn = createControlButton("⏭", e -> playerService.playNext());
        Button stopBtn = createControlButton("⏹", e -> playerService.stop());

        buttons.getChildren().addAll(prevBtn, playPauseBtn, nextBtn, stopBtn);

        // Progress bar
        HBox progressBox = new HBox(10);
        progressBox.setAlignment(Pos.CENTER);
        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        currentTimeLabel.setPrefWidth(35);

        progressSlider = new Slider(0, 100, 0);
        progressSlider.setStyle("-fx-control-inner-background: #e5e5ea;");
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        progressSlider.setOnMousePressed(e -> playerService.pause());
        progressSlider.setOnMouseReleased(e -> {
            playerService.seek(progressSlider.getValue());
            if (playerService.isPlaying()) playerService.play();
        });

        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        totalTimeLabel.setPrefWidth(35);

        progressBox.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);

        controlsBox.getChildren().addAll(buttons, progressBox);
        return controlsBox;
    }

    /**
     * Create right side controls (volume and mode)
     */
    private HBox createRightControls() {
        HBox rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setPrefWidth(150);

        // Volume slider
        Label volumeIcon = new Label("🔊");
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(100);
        volumeSlider.setStyle("-fx-control-inner-background: #e5e5ea;");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            playerService.setVolume(newVal.doubleValue())
        );

        // Play mode combo
        playModeCombo = new ComboBox<>();
        playModeCombo.getItems().setAll(
                MusicPlayerService.PlayMode.SEQUENTIAL,
                MusicPlayerService.PlayMode.LOOP_ONE,
                MusicPlayerService.PlayMode.LOOP_ALL,
                MusicPlayerService.PlayMode.SHUFFLE
        );
        playModeCombo.setValue(MusicPlayerService.PlayMode.SEQUENTIAL);
        playModeCombo.setPrefWidth(120);
        playModeCombo.setStyle("-fx-font-size: 11;");
        playModeCombo.setOnAction(e -> playerService.setPlayMode(playModeCombo.getValue()));

        rightBox.getChildren().addAll(volumeIcon, volumeSlider, playModeCombo);
        return rightBox;
    }

    /**
     * Create a control button
     */
    private Button createControlButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-font-size: 14; " +
            "-fx-padding: 8 12; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #007AFF; " +
            "-fx-cursor: hand;"
        );
        btn.setOnAction(handler);
        return btn;
    }

    /**
     * Toggle play/pause
     */
    private void togglePlayPause() {
        if (playerService.isPlaying()) {
            playerService.pause();
        } else {
            playerService.play();
        }
    }

    /**
     * Start background thread for progress updates
     */
    private void startProgressUpdater() {
        Thread progressThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    Platform.runLater(() -> {
                        if (playerService.isPlaying()) {
                            double currentTime = playerService.getCurrentTime();
                            double totalTime = playerService.getTotalDuration();
                            if (totalTime > 0) {
                                progressSlider.setValue(currentTime);
                                progressSlider.setMax(totalTime);
                                currentTimeLabel.setText(formatTime(currentTime));
                                totalTimeLabel.setText(formatTime(totalTime));
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        progressThread.setDaemon(true);
        progressThread.start();
    }

    /**
     * Format seconds to MM:SS
     */
    private String formatTime(double seconds) {
        int totalSeconds = (int) seconds;
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    /**
     * Get the control bar component
     */
    public HBox getControlBar() {
        return controlBar;
    }

    @Override
    public void onPlaybackStatusChanged(boolean isPlaying) {
        Platform.runLater(() -> {
            playPauseBtn.setText(isPlaying ? "⏸" : "▶");
        });
    }

    @Override
    public void onCurrentSongChanged(Song song) {
        Platform.runLater(() -> {
            if (song != null) {
                songTitleLabel.setText(song.getTitle());
                artistLabel.setText(song.getArtist());
                
                // Load album art
                if (song.getCoverPath() != null && !song.getCoverPath().isEmpty()) {
                    try {
                        File coverFile = new File(song.getCoverPath());
                        if (coverFile.exists()) {
                            albumArtSmall.setImage(new Image(coverFile.toURI().toString()));
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading cover: " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onProgressChanged(double currentTime, double totalTime) {
        // Handled by progress updater thread
    }

    @Override
    public void onQueueChanged() {
        // Update UI if queue changes
    }
}
