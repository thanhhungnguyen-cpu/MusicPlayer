package com.musicplayer.dao;

import com.musicplayer.models.User;

import java.io.*;
import java.util.*;

/**
 * Data Access Object (DAO) for User persistence
 * Handles user data storage and retrieval for access control and analytics
 */
public class UserRepository {
    private final String filePath;

    public UserRepository(String filePath) {
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
                System.err.println("Error creating user data file: " + e.getMessage());
            }
        }
    }

    /**
     * Save all users to file
     */
    public void saveUsers(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(users);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Load all users from file
     */
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        
        if (new File(filePath).length() == 0) {
            return users;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            users = (List<User>) ois.readObject();
        } catch (EOFException e) {
            System.out.println("User data file is empty or corrupted, starting fresh");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Create a new user
     */
    public void createUser(User user) {
        List<User> users = loadUsers();
        if (!users.contains(user)) {
            users.add(user);
            saveUsers(users);
        }
    }

    /**
     * Update an existing user
     */
    public void updateUser(User user) {
        List<User> users = loadUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                saveUsers(users);
                return;
            }
        }
    }

    /**
     * Get a user by ID
     */
    public User getUserById(String userId) {
        List<User> users = loadUsers();
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get a user by username
     */
    public User getUserByUsername(String username) {
        List<User> users = loadUsers();
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return loadUsers();
    }

    /**
     * Get all active users
     */
    public List<User> getActiveUsers() {
        List<User> users = loadUsers();
        return users.stream()
                .filter(User::isActive)
                .toList();
    }

    /**
     * Get all blocked users
     */
    public List<User> getBlockedUsers() {
        List<User> users = loadUsers();
        return users.stream()
                .filter(User::isBlocked)
                .toList();
    }

    /**
     * Block a user
     */
    public void blockUser(String userId) {
        User user = getUserById(userId);
        if (user != null) {
            user.setBlocked(true);
            updateUser(user);
        }
    }

    /**
     * Unblock a user
     */
    public void unblockUser(String userId) {
        User user = getUserById(userId);
        if (user != null) {
            user.setBlocked(false);
            updateUser(user);
        }
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(User.UserRole role) {
        List<User> users = loadUsers();
        return users.stream()
                .filter(u -> u.getRole() == role)
                .toList();
    }

    /**
     * Clear all users
     */
    public void clearAll() {
        saveUsers(new ArrayList<>());
    }

    /**
     * Get total number of users
     */
    public int getUserCount() {
        return loadUsers().size();
    }
}
