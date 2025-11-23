package com.example.keycloak.storage;

import com.example.keycloak.storage.database.DatabaseConnectionManager;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

/**
 * External User Storage Provider Factory
 * Creates and configures provider instances
 */
public class ExternalUserStorageProviderFactory implements UserStorageProviderFactory<ExternalUserStorageProvider> {

    private static final Logger logger = Logger.getLogger(ExternalUserStorageProviderFactory.class);

    public static final String PROVIDER_ID = "external-user-storage";

    // Configuration property keys
    private static final String CONFIG_KEY_DB_HOST = "dbHost";
    private static final String CONFIG_KEY_DB_PORT = "dbPort";
    private static final String CONFIG_KEY_DB_NAME = "dbName";
    private static final String CONFIG_KEY_DB_USER = "dbUser";
    private static final String CONFIG_KEY_DB_PASSWORD = "dbPassword";

    // Default values
    private static final String DEFAULT_DB_HOST = "external-user-db";
    private static final String DEFAULT_DB_PORT = "5432";
    private static final String DEFAULT_DB_NAME = "userdb";
    private static final String DEFAULT_DB_USER = "userapp";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public ExternalUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        logger.infof("Creating External User Storage Provider instance: %s", model.getName());

        // Get configuration from environment variables or component config
        String dbHost = getConfigValue(model, CONFIG_KEY_DB_HOST,
                                      System.getenv("EXTERNAL_DB_HOST"), DEFAULT_DB_HOST);
        String dbPort = getConfigValue(model, CONFIG_KEY_DB_PORT,
                                      System.getenv("EXTERNAL_DB_PORT"), DEFAULT_DB_PORT);
        String dbName = getConfigValue(model, CONFIG_KEY_DB_NAME,
                                      System.getenv("EXTERNAL_DB_NAME"), DEFAULT_DB_NAME);
        String dbUser = getConfigValue(model, CONFIG_KEY_DB_USER,
                                      System.getenv("EXTERNAL_DB_USER"), DEFAULT_DB_USER);
        String dbPassword = getConfigValue(model, CONFIG_KEY_DB_PASSWORD,
                                          System.getenv("EXTERNAL_DB_PASSWORD"), "");

        logger.infof("Database configuration: %s:%s/%s", dbHost, dbPort, dbName);

        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(
            dbHost, dbPort, dbName, dbUser, dbPassword
        );

        // Test connection
        if (!connectionManager.testConnection()) {
            logger.errorf("Failed to connect to external database: %s:%s/%s", dbHost, dbPort, dbName);
            throw new RuntimeException("Cannot connect to external user database");
        }

        return new ExternalUserStorageProvider(session, model, connectionManager);
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
            throws ComponentValidationException {
        logger.infof("Validating configuration for: %s", config.getName());

        String dbHost = getConfigValue(config, CONFIG_KEY_DB_HOST, null, DEFAULT_DB_HOST);
        String dbPort = getConfigValue(config, CONFIG_KEY_DB_PORT, null, DEFAULT_DB_PORT);
        String dbName = getConfigValue(config, CONFIG_KEY_DB_NAME, null, DEFAULT_DB_NAME);
        String dbUser = getConfigValue(config, CONFIG_KEY_DB_USER, null, DEFAULT_DB_USER);
        String dbPassword = getConfigValue(config, CONFIG_KEY_DB_PASSWORD, null, "");

        if (dbHost == null || dbHost.isEmpty()) {
            throw new ComponentValidationException("Database host is required");
        }
        if (dbPort == null || dbPort.isEmpty()) {
            throw new ComponentValidationException("Database port is required");
        }
        if (dbName == null || dbName.isEmpty()) {
            throw new ComponentValidationException("Database name is required");
        }
        if (dbUser == null || dbUser.isEmpty()) {
            throw new ComponentValidationException("Database user is required");
        }

        // Validate port number
        try {
            int port = Integer.parseInt(dbPort);
            if (port < 1 || port > 65535) {
                throw new ComponentValidationException("Invalid port number: " + dbPort);
            }
        } catch (NumberFormatException e) {
            throw new ComponentValidationException("Port must be a valid number");
        }

        // Test database connection
        try {
            DatabaseConnectionManager testConnectionManager = new DatabaseConnectionManager(
                dbHost, dbPort, dbName, dbUser, dbPassword
            );

            if (!testConnectionManager.testConnection()) {
                throw new ComponentValidationException(
                    "Cannot connect to external database. Please check configuration."
                );
            }
            logger.info("Database connection test successful");
        } catch (Exception e) {
            logger.error("Database connection validation failed", e);
            throw new ComponentValidationException("Database connection failed: " + e.getMessage());
        }
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
            .property()
                .name(CONFIG_KEY_DB_HOST)
                .label("Database Host")
                .helpText("External database hostname or IP address")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(DEFAULT_DB_HOST)
                .add()
            .property()
                .name(CONFIG_KEY_DB_PORT)
                .label("Database Port")
                .helpText("External database port number")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(DEFAULT_DB_PORT)
                .add()
            .property()
                .name(CONFIG_KEY_DB_NAME)
                .label("Database Name")
                .helpText("External database name")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(DEFAULT_DB_NAME)
                .add()
            .property()
                .name(CONFIG_KEY_DB_USER)
                .label("Database User")
                .helpText("Database username for authentication")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(DEFAULT_DB_USER)
                .add()
            .property()
                .name(CONFIG_KEY_DB_PASSWORD)
                .label("Database Password")
                .helpText("Database password for authentication")
                .type(ProviderConfigProperty.PASSWORD)
                .secret(true)
                .add()
            .build();
    }

    @Override
    public String getHelpText() {
        return "External User Storage Provider - Connects to external PostgreSQL database for user authentication";
    }

    /**
     * Helper method to get configuration value with fallback priority:
     * 1. Component configuration
     * 2. Environment variable
     * 3. Default value
     */
    private String getConfigValue(ComponentModel config, String key, String envValue, String defaultValue) {
        String configValue = config.get(key);
        if (configValue != null && !configValue.isEmpty()) {
            return configValue;
        }
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return defaultValue;
    }
}
