package com.example.keycloak.storage.database;

import com.example.keycloak.storage.model.ExternalUser;
import org.jboss.logging.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User Repository - handles database operations for external users
 */
public class UserRepository {
    private static final Logger logger = Logger.getLogger(UserRepository.class);

    private final DatabaseConnectionManager connectionManager;

    public UserRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Find user by username
     */
    public ExternalUser findByUsername(String username) {
        String sql = "SELECT id, username, email, first_name, last_name, password_hash, enabled, " +
                    "created_at, updated_at FROM users WHERE username = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.errorf(e, "Error finding user by username: %s", username);
        }
        return null;
    }

    /**
     * Find user by email
     */
    public ExternalUser findByEmail(String email) {
        String sql = "SELECT id, username, email, first_name, last_name, password_hash, enabled, " +
                    "created_at, updated_at FROM users WHERE email = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.errorf(e, "Error finding user by email: %s", email);
        }
        return null;
    }

    /**
     * Find user by ID
     */
    public ExternalUser findById(Long id) {
        String sql = "SELECT id, username, email, first_name, last_name, password_hash, enabled, " +
                    "created_at, updated_at FROM users WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.errorf(e, "Error finding user by id: %s", id);
        }
        return null;
    }

    /**
     * Search users by username or email
     */
    public List<ExternalUser> searchUsers(String searchTerm, int firstResult, int maxResults) {
        List<ExternalUser> users = new ArrayList<>();
        String sql = "SELECT id, username, email, first_name, last_name, password_hash, enabled, " +
                    "created_at, updated_at FROM users WHERE username ILIKE ? OR email ILIKE ? " +
                    "ORDER BY username LIMIT ? OFFSET ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setInt(3, maxResults);
            stmt.setInt(4, firstResult);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.errorf(e, "Error searching users with term: %s", searchTerm);
        }
        return users;
    }

    /**
     * Get all users with pagination
     */
    public List<ExternalUser> getAllUsers(int firstResult, int maxResults) {
        List<ExternalUser> users = new ArrayList<>();
        String sql = "SELECT id, username, email, first_name, last_name, password_hash, enabled, " +
                    "created_at, updated_at FROM users ORDER BY username LIMIT ? OFFSET ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maxResults);
            stmt.setInt(2, firstResult);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting all users", e);
        }
        return users;
    }

    /**
     * Get total user count
     */
    public int getUsersCount() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting users", e);
        }
        return 0;
    }

    /**
     * Validate user credentials
     */
    public boolean validateCredentials(String username, String password) {
        ExternalUser user = findByUsername(username);
        if (user == null || !user.isEnabled()) {
            return false;
        }

        // BCrypt password validation
        return PasswordHasher.verifyPassword(password, user.getPasswordHash());
    }

    /**
     * Map ResultSet to ExternalUser object
     */
    private ExternalUser mapResultSetToUser(ResultSet rs) throws SQLException {
        ExternalUser user = new ExternalUser();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
