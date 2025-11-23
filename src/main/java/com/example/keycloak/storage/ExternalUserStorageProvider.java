package com.example.keycloak.storage;

import com.example.keycloak.storage.adapter.ExternalUserAdapter;
import com.example.keycloak.storage.database.DatabaseConnectionManager;
import com.example.keycloak.storage.database.UserRepository;
import com.example.keycloak.storage.model.ExternalUser;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.Provider;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.util.Map;
import java.util.stream.Stream;

/**
 * External User Storage Provider - main provider implementation
 * Implements user lookup, search, and credential validation
 */
public class ExternalUserStorageProvider implements
        Provider,
        UserLookupProvider,
        UserQueryProvider,
        CredentialInputValidator {

    private static final Logger logger = Logger.getLogger(ExternalUserStorageProvider.class);

    protected final KeycloakSession session;
    protected final ComponentModel model;
    private final UserRepository userRepository;

    public ExternalUserStorageProvider(KeycloakSession session, ComponentModel model,
                                      DatabaseConnectionManager connectionManager) {
        this.session = session;
        this.model = model;
        this.userRepository = new UserRepository(connectionManager);
        logger.infof("External User Storage Provider initialized for model: %s", model.getName());
    }

    @Override
    public void close() {
        logger.debug("Closing External User Storage Provider");
    }

    // ===== UserLookupProvider Implementation =====

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.debugf("Looking up user by ID: %s", id);
        String externalId = StorageId.externalId(id);
        try {
            Long userId = Long.parseLong(externalId);
            ExternalUser externalUser = userRepository.findById(userId);
            if (externalUser != null) {
                return new ExternalUserAdapter(session, realm, model, externalUser);
            }
        } catch (NumberFormatException e) {
            logger.errorf("Invalid user ID format: %s", externalId);
        }
        return null;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.debugf("Looking up user by username: %s", username);
        ExternalUser externalUser = userRepository.findByUsername(username);
        if (externalUser != null) {
            return new ExternalUserAdapter(session, realm, model, externalUser);
        }
        logger.debugf("User not found: %s", username);
        return null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.debugf("Looking up user by email: %s", email);
        ExternalUser externalUser = userRepository.findByEmail(email);
        if (externalUser != null) {
            return new ExternalUserAdapter(session, realm, model, externalUser);
        }
        logger.debugf("User not found with email: %s", email);
        return null;
    }

    // ===== UserQueryProvider Implementation =====

    @Override
    public int getUsersCount(RealmModel realm) {
        logger.debug("Getting total users count");
        return userRepository.getUsersCount();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        logger.debugf("Searching users with term: %s (first=%d, max=%d)", search, firstResult, maxResults);
        return userRepository.searchUsers(search, firstResult, maxResults).stream()
                .map(user -> new ExternalUserAdapter(session, realm, model, user));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        logger.debugf("Searching users with params: %s", params);

        String search = params.get(UserModel.SEARCH);
        if (search != null) {
            return searchForUserStream(realm, search, firstResult, maxResults);
        }

        String username = params.get(UserModel.USERNAME);
        if (username != null) {
            UserModel user = getUserByUsername(realm, username);
            return user != null ? Stream.of(user) : Stream.empty();
        }

        String email = params.get(UserModel.EMAIL);
        if (email != null) {
            UserModel user = getUserByEmail(realm, email);
            return user != null ? Stream.of(user) : Stream.empty();
        }

        // Default: return all users
        return getUsersStream(realm, firstResult, maxResults);
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        // External database doesn't support group membership
        logger.debug("Group membership not supported in external database");
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        // External database custom attributes not implemented
        logger.debugf("User attribute search not implemented: %s=%s", attrName, attrValue);
        return Stream.empty();
    }

    public Stream<UserModel> getUsersStream(RealmModel realm, Integer firstResult, Integer maxResults) {
        logger.debugf("Getting all users (first=%d, max=%d)", firstResult, maxResults);
        return userRepository.getAllUsers(firstResult, maxResults).stream()
                .map(user -> new ExternalUserAdapter(session, realm, model, user));
    }

    // ===== CredentialInputValidator Implementation =====

    @Override
    public boolean supportsCredentialType(String credentialType) {
        boolean supported = PasswordCredentialModel.TYPE.equals(credentialType);
        logger.debugf("Credential type '%s' supported: %s", credentialType, supported);
        return supported;
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!supportsCredentialType(credentialInput.getType())) {
            logger.debugf("Credential type not supported: %s", credentialInput.getType());
            return false;
        }

        String username = user.getUsername();
        String password = credentialInput.getChallengeResponse();

        logger.debugf("Validating credentials for user: %s", username);
        boolean isValid = userRepository.validateCredentials(username, password);
        logger.debugf("Credential validation result for '%s': %s", username, isValid);

        return isValid;
    }
}
