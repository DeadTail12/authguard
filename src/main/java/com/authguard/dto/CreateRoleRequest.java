package com.authguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank String name,
        @NotEmpty Set<String> permissionNames) {
}
