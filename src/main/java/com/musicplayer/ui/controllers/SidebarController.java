package com.musicplayer.ui.controllers;

import javafx.scene.layout.VBox;

/**
 * Controller for sidebar navigation
 * Manages navigation between browse, library, and playlists views
 */
public class SidebarController {
    private VBox sidebar;

    public SidebarController() {
        // Sidebar initialization handled in main controller
    }

    public VBox getSidebar() {
        return sidebar;
    }
}
