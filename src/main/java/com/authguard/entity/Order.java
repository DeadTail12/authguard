package com.authguard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** The resource ABAC decisions are demonstrated against: it has a region and an owner. */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String region;

    private String ownerKeycloakUserId;

    private String description;

    protected Order() {
        // required by JPA
    }

    public Order(String region, String ownerKeycloakUserId, String description) {
        this.region = region;
        this.ownerKeycloakUserId = ownerKeycloakUserId;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getOwnerKeycloakUserId() {
        return ownerKeycloakUserId;
    }

    public String getDescription() {
        return description;
    }
}
