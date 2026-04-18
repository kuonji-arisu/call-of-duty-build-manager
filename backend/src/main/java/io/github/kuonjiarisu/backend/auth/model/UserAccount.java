package io.github.kuonjiarisu.backend.auth.model;

import java.time.LocalDateTime;

public record UserAccount(
    String id,
    String username,
    String passwordHash,
    String role,
    String displayName,
    Boolean enabled,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
