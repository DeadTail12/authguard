package com.authguard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Grants one role to one identity. There is deliberately no "User" entity here: identity
 * lives in Keycloak, so we key this purely on the JWT "sub" claim.
 */
@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keycloakUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected UserRole() {
        // required by JPA
    }

    public UserRole(String keycloakUserId, Role role) {
        this.keycloakUserId = keycloakUserId;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public Role getRole() {
        return role;
    }
}
