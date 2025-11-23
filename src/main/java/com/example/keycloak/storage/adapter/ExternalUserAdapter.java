package com.example.keycloak.storage.adapter;

import com.example.keycloak.storage.model.ExternalUser;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapter;

/**
 * User Adapter - bridges external user data to Keycloak user model
 * Uses AbstractUserAdapter for Keycloak 23.0 compatibility
 */
public class ExternalUserAdapter extends AbstractUserAdapter {
    private final ExternalUser externalUser;
    private final String keycloakId;

    public ExternalUserAdapter(KeycloakSession session, RealmModel realm,
                              ComponentModel model, ExternalUser externalUser) {
        super(session, realm, model);
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
    public String getEmail() {
        return externalUser.getEmail();
    }

    @Override
    public void setEmail(String email) {
        externalUser.setEmail(email);
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
    public boolean isEnabled() {
        return externalUser.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        externalUser.setEnabled(enabled);
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        // Return a default implementation
        return new SubjectCredentialManager() {
            @Override
            public boolean isValid(org.keycloak.models.credential.CredentialInput input) {
                return false;
            }

            @Override
            public boolean updateCredential(org.keycloak.models.credential.CredentialInput input) {
                return false;
            }

            @Override
            public void updateStoredCredential(org.keycloak.credential.CredentialModel cred) {
            }

            @Override
            public org.keycloak.credential.CredentialModel createStoredCredential(org.keycloak.credential.CredentialModel cred) {
                return null;
            }

            @Override
            public boolean removeStoredCredentialById(String id) {
                return false;
            }

            @Override
            public org.keycloak.credential.CredentialModel getStoredCredentialById(String id) {
                return null;
            }

            @Override
            public java.util.stream.Stream<org.keycloak.credential.CredentialModel> getStoredCredentialsStream() {
                return java.util.stream.Stream.empty();
            }

            @Override
            public java.util.stream.Stream<org.keycloak.credential.CredentialModel> getStoredCredentialsByTypeStream(String type) {
                return java.util.stream.Stream.empty();
            }

            @Override
            public org.keycloak.credential.CredentialModel getStoredCredentialByNameAndType(String name, String type) {
                return null;
            }

            @Override
            public boolean moveStoredCredentialTo(String id, String newPreviousCredentialId) {
                return false;
            }

            @Override
            public void updateCredentialLabel(String credentialId, String label) {
            }

            @Override
            public void disableCredentialType(String credentialType) {
            }

            @Override
            public java.util.stream.Stream<String> getDisableableCredentialTypesStream() {
                return java.util.stream.Stream.empty();
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
            public java.util.stream.Stream<String> getConfiguredUserStorageCredentialTypesStream() {
                return java.util.stream.Stream.empty();
            }

        };
    }

    public ExternalUser getExternalUser() {
        return externalUser;
    }
}
