package com.authguard.dto;

/** What we tell a caller about themselves — only claims we trust from a verified JWT. */
public record MeResponse(String userId, String username, String email) {
}
