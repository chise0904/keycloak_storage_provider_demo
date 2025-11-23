package com.example.keycloak.storage.adapter;

import com.example.keycloak.storage.model.ExternalUser;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

/**
 * User Adapter - bridges external user data to Keycloak user model
 */
public class ExternalUserAdapter extends AbstractUserAdapterFederatedStorage {
    private final ExternalUser externalUser;

    public ExternalUserAdapter(KeycloakSession session, RealmModel realm,
                              ComponentModel model, ExternalUser externalUser) {
        super(session, realm, model);
        this.externalUser = externalUser;
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

    public ExternalUser getExternalUser() {
        return externalUser;
    }
}
