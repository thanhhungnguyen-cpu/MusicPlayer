package com.musicplayer.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model class representing a User with role-based permissions
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum UserRole {
        ADMIN, VIP, STANDARD
    }

    private String id;
    private String username;
    private String email;
    private UserRole role;
    private boolean isActive;
    private boolean isBlocked;
    private LocalDateTime dateCreated;
    private LocalDateTime lastLoginDate;
    private long totalListenTime; // in seconds

    public User(String id, String username, String email, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isActive = true;
        this.isBlocked = false;
        this.dateCreated = LocalDateTime.now();
        this.totalListenTime = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public LocalDateTime getDateCreated() { return dateCreated; }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public long getTotalListenTime() { return totalListenTime; }
    public void addListenTime(long seconds) { this.totalListenTime += seconds; }

    public boolean hasAdminPrivileges() {
        return role == UserRole.ADMIN;
    }

    public boolean isVip() {
        return role == UserRole.VIP || role == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", isBlocked=" + isBlocked +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
