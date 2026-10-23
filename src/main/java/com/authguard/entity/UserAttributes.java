package com.authguard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** ABAC attributes AuthGuard itself tracks about a Keycloak identity — just region for now. */
@Entity
@Table(name = "user_attributes")
public class UserAttributes {

    @Id
    private String keycloakUserId;

    private String region;

    protected UserAttributes() {
        // required by JPA
    }

    public UserAttributes(String keycloakUserId, String region) {
        this.keycloakUserId = keycloakUserId;
        this.region = region;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public String getRegion() {
        return region;
    }
}
