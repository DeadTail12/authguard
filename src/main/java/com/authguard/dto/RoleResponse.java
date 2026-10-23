package com.authguard.dto;

import java.util.Set;

public record RoleResponse(Long id, String name, Set<String> permissions) {
}
