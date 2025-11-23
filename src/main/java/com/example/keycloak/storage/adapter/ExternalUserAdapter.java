package com.example.keycloak.storage.adapter;

import com.example.keycloak.storage.model.ExternalUser;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;

import java.util.*;
import java.util.stream.Stream;

/**
 * User Adapter - bridges external user data to Keycloak user model
 * Direct implementation of UserModel for Keycloak 23.0
 */
public class ExternalUserAdapter implements UserModel {
    private final KeycloakSession session;
    private final RealmModel realm;
    private final ComponentModel storageProviderModel;
    private final ExternalUser externalUser;
    private final String keycloakId;

    public ExternalUserAdapter(KeycloakSession session, RealmModel realm,
                              ComponentModel model, ExternalUser externalUser) {
        this.session = session;
        this.realm = realm;
        this.storageProviderModel = model;
        this.externalUser = externalUser;
        this.keycloakId = StorageId.keycloakId(model, String.valueOf(externalUser.getId()));
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public String getUsername() {
        return externalUser.getUsername();
    }

    @Override
    public void setUsername(String username) {
        externalUser.setUsername(username);
    }

    @Override
    public Long getCreatedTimestamp() {
        return externalUser.getCreatedAt() != null ? externalUser.getCreatedAt().getTime() : null;
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        if (timestamp != null) {
            externalUser.setCreatedAt(new Date(timestamp));
        }
    }

    @Override
    public boolean isEnabled() {
        return externalUser.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        externalUser.setEnabled(enabled);
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        // Not implemented - external database doesn't support custom attributes
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        // Not implemented - external database doesn't support custom attributes
    }

    @Override
    public void removeAttribute(String name) {
        // Not implemented - external database doesn't support custom attributes
    }

    @Override
    public String getFirstAttribute(String name) {
        // Return basic attributes
        switch (name) {
            case "firstName": return getFirstName();
            case "lastName": return getLastName();
            case "email": return getEmail();
            case "username": return getUsername();
            default: return null;
        }
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        String value = getFirstAttribute(name);
        return value != null ? Stream.of(value) : Stream.empty();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attributes = new HashMap<>();
        if (getFirstName() != null) attributes.put("firstName", Collections.singletonList(getFirstName()));
        if (getLastName() != null) attributes.put("lastName", Collections.singletonList(getLastName()));
        if (getEmail() != null) attributes.put("email", Collections.singletonList(getEmail()));
        if (getUsername() != null) attributes.put("username", Collections.singletonList(getUsername()));
        return attributes;
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return Stream.empty();
    }

    @Override
    public void addRequiredAction(String action) {
        // Not implemented
    }

    @Override
    public void removeRequiredAction(String action) {
        // Not implemented
    }

    @Override
    public String getFirstName() {
        return externalUser.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        externalUser.setFirstName(firstName);
    }

    @Override
    public String getLastName() {
        return externalUser.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        externalUser.setLastName(lastName);
    }

    @Override
    public String getEmail() {
        return externalUser.getEmail();
    }

    @Override
    public void setEmail(String email) {
        externalUser.setEmail(email);
    }

    @Override
    public boolean isEmailVerified() {
        return true; // Assume email is verified for external users
    }

    @Override
    public void setEmailVerified(boolean verified) {
        // Not implemented
    }

    @Override
    public Stream<GroupModel> getGroupsStream() {
        return Stream.empty();
    }

    @Override
    public void joinGroup(GroupModel group) {
        // Not implemented
    }

    @Override
    public void leaveGroup(GroupModel group) {
        // Not implemented
    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        return false;
    }

    @Override
    public String getFederationLink() {
        return null;
    }

    @Override
    public void setFederationLink(String link) {
        // Not implemented
    }

    @Override
    public String getServiceAccountClientLink() {
        return null;
    }

    @Override
    public void setServiceAccountClientLink(String clientInternalId) {
        // Not implemented
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return Stream.empty();
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
        return Stream.empty();
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return false;
    }

    @Override
    public void grantRole(RoleModel role) {
        // Not implemented
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return Stream.empty();
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        // Not implemented
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        // Return a minimal implementation that delegates to the provider
        return new SubjectCredentialManager() {
            @Override
            public boolean isValid(List<CredentialInput> inputs) {
                return false;
            }

            @Override
            public boolean updateCredential(CredentialInput input) {
                return false;
            }

            @Override
            public void updateStoredCredential(CredentialModel cred) {
            }

            @Override
            public CredentialModel createStoredCredential(CredentialModel cred) {
                return null;
            }

            @Override
            public boolean removeStoredCredentialById(String id) {
                return false;
            }

            @Override
            public CredentialModel getStoredCredentialById(String id) {
                return null;
            }

            @Override
            public Stream<CredentialModel> getStoredCredentialsStream() {
                return Stream.empty();
            }

            @Override
            public Stream<CredentialModel> getStoredCredentialsByTypeStream(String type) {
                return Stream.empty();
            }

            @Override
            public CredentialModel getStoredCredentialByNameAndType(String name, String type) {
                return null;
            }

            @Override
            public boolean moveStoredCredentialTo(String id, String newPreviousCredentialId) {
                return false;
            }

            @Override
            public void updateCredentialLabel(String credentialId, String userLabel) {
            }

            @Override
            public void disableCredentialType(String credentialType) {
            }

            @Override
            public Stream<String> getDisableableCredentialTypesStream() {
                return Stream.empty();
            }

            @Override
            public boolean isConfiguredFor(String type) {
                return false;
            }

            @Override
            public boolean isConfiguredLocally(String type) {
                return false;
            }

            @Override
            public Stream<String> getConfiguredUserStorageCredentialTypesStream() {
                return Stream.empty();
            }

            @Override
            public CredentialModel createCredentialThroughProvider(CredentialModel model) {
                return null;
            }
        };
    }

    public ExternalUser getExternalUser() {
        return externalUser;
    }
}
