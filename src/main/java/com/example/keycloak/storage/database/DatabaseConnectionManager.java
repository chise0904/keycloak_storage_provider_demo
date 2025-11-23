package com.example.keycloak.storage.database;

import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Connection Manager - handles connections to external user database
 */
public class DatabaseConnectionManager {
    private static final Logger logger = Logger.getLogger(DatabaseConnectionManager.class);

    private final String dbHost;
    private final String dbPort;
    private final String dbName;
    private final String dbUser;
    private final String dbPassword;
    private final String jdbcUrl;

    public DatabaseConnectionManager(String dbHost, String dbPort, String dbName,
                                    String dbUser, String dbPassword) {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);

        logger.infof("Initializing database connection manager: %s", jdbcUrl);
    }

    /**
     * Get a database connection
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL JDBC Driver not found", e);
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }

        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        props.setProperty("ssl", "false");

        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, props);
            logger.debugf("Successfully connected to database: %s", jdbcUrl);
            return connection;
        } catch (SQLException e) {
            logger.errorf(e, "Failed to connect to database: %s", jdbcUrl);
            throw e;
        }
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.errorf(e, "Database connection test failed");
            return false;
        }
    }

    /**
     * Close connection safely
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
