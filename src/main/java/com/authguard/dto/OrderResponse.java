package com.authguard.dto;

public record OrderResponse(Long id, String region, String ownerUserId, String description) {
}
