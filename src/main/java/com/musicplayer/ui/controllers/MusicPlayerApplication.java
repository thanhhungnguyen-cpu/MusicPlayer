package com.musicplayer.ui.controllers;

import com.musicplayer.models.Song;
import com.musicplayer.models.Playlist;
import com.musicplayer.services.*;
import com.musicplayer.dao.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;

/**
 * Main Application Controller for Music Player
 * Orchestrates the entire UI and service layer
 * Follows Apple Music aesthetic with modern JavaFX design
 */
public class MusicPlayerApplication extends Application {
    private Stage primaryStage;
    private Scene mainScene;

    // Services
    private SongService songService;
    private PlaylistService playlistService;
    private MusicPlayerService playerService;
    private AnalyticsService analyticsService;

    // UI Components
    private SongGridController songGridController;
    private PlaylistPanelController playlistPanelController;
    private BottomPlayerController bottomPlayerController;
    private SidebarController sidebarController;

    // Data Access Objects
    private SongRepository songRepository;
    private PlaylistRepository playlistRepository;
    private ListeningStatsRepository statsRepository;
    private UserRepository userRepository;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeRepositories();
        initializeServices();
        createUI();
        loadSampleData();

        primaryStage.setTitle("Apple Music Style Player");
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    /**
     * Initialize all data repositories
     */
    private void initializeRepositories() {
        String dataDir = System.getProperty("user.home") + File.separator + ".musicplayer";
        songRepository = new SongRepository(dataDir + File.separator + "songs.dat");
        playlistRepository = new PlaylistRepository(dataDir + File.separator + "playlists.dat");
        statsRepository = new ListeningStatsRepository(dataDir + File.separator + "stats.dat");
        userRepository = new UserRepository(dataDir + File.separator + "users.dat");
    }

    /**
     * Initialize all service layers
     */
    private void initializeServices() {
        songService = new SongService(songRepository);
        playlistService = new PlaylistService(playlistRepository, songRepository);
        playerService = new MusicPlayerService(songRepository, playlistRepository, statsRepository);
        analyticsService = new AnalyticsService(statsRepository, songRepository);
    }

    /**
     * Create the main UI layout
     */
    private void createUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f7;");

        // Top menu bar
        root.setTop(createMenuBar());

        // Main content area with sidebar and center
        HBox mainContent = new HBox();
        mainContent.setSpacing(0);

        // Left Sidebar
        VBox sidebar = createSidebar();
        HBox.setHgrow(sidebar, Priority.NEVER);
        mainContent.getChildren().add(sidebar);

        // Center content area
        VBox centerContent = new VBox();
        centerContent.setStyle("-fx-background-color: #ffffff;");
        HBox.setHgrow(centerContent, Priority.ALWAYS);

        // Song grid
        songGridController = new SongGridController(songService, playerService);
        VBox.setVgrow(songGridController.getGridPane(), Priority.ALWAYS);
        centerContent.getChildren().add(songGridController.getGridPane());

        mainContent.getChildren().add(centerContent);

        // Bottom player control
        bottomPlayerController = new BottomPlayerController(playerService, songService);
        root.setCenter(mainContent);
        root.setBottom(bottomPlayerController.getControlBar());

        // Setup player service listener
        playerService.addPlaybackListener(bottomPlayerController);

        mainScene = new Scene(root);
        mainScene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
    }

    /**
     * Create menu bar
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f7;");

        Menu fileMenu = new Menu("File");
        MenuItem importSongs = new MenuItem("Import Songs");
        importSongs.setOnAction(e -> showImportDialog());
        fileMenu.getItems().addAll(importSongs, new SeparatorMenuItem(), 
                new MenuItem("Exit"));

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
                new MenuItem("Create Playlist"),
                new MenuItem("Preferences")
        );

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(
                new MenuItem("Browse"),
                new MenuItem("Library"),
                new MenuItem("Playlists")
        );

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);
        return menuBar;
    }

    /**
     * Create left sidebar with navigation
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #f5f5f7; -fx-border-right: 1px solid #e5e5ea;");
        sidebar.setPrefWidth(250);

        // Logo
        Label logo = new Label("🎵 Music Player");
        logo.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #000;");
        sidebar.getChildren().add(logo);

        // Navigation buttons
        Button browseBtn = createNavButton("Discover", "explore-icon");
        Button libraryBtn = createNavButton("Library", "library-icon");
        Button playlistsBtn = createNavButton("Playlists", "playlist-icon");

        sidebar.getChildren().addAll(
                new Separator(),
                browseBtn,
                libraryBtn,
                playlistsBtn
        );

        // Playlists section
        Label playlistsLabel = new Label("Your Playlists");
        playlistsLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #666;");
        sidebar.getChildren().add(new Separator());
        sidebar.getChildren().add(playlistsLabel);

        // Playlist list
        playlistPanelController = new PlaylistPanelController(playlistService, playerService);
        VBox.setVgrow(playlistPanelController.getPlaylistList(), Priority.ALWAYS);
        sidebar.getChildren().add(playlistPanelController.getPlaylistList());

        VBox.setVgrow(sidebar, Priority.ALWAYS);
        return sidebar;
    }

    /**
     * Create navigation button
     */
    private Button createNavButton(String text, String icon) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-padding: 10 15; " +
            "-fx-font-size: 13; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #000; " +
            "-fx-cursor: hand; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    /**
     * Load sample data for demo
     */
    private void loadSampleData() {
        if (songRepository.getSongCount() == 0) {
            // Create sample songs (using placeholder paths)
            songService.addSong("Song 1", "Artist 1", "Album 1", "Pop", 180, 
                    "/path/to/song1.mp3", "/path/to/cover1.jpg");
            songService.addSong("Song 2", "Artist 2", "Album 2", "Rock", 240, 
                    "/path/to/song2.mp3", "/path/to/cover2.jpg");
            songService.addSong("Song 3", "Artist 1", "Album 1", "Pop", 200, 
                    "/path/to/song3.mp3", "/path/to/cover3.jpg");

            // Refresh UI
            songGridController.refreshGrid();
        }
    }

    /**
     * Show import dialog
     */
    private void showImportDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Import Songs");
        alert.setHeaderText("Import your music files");
        alert.setContentText("Select a folder containing MP3 or WAV files.");
        alert.showAndWait();
    }

    @Override
    public void stop() {
        playerService.shutdown();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
