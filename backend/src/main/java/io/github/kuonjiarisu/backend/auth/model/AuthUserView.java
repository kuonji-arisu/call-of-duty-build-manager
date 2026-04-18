package io.github.kuonjiarisu.backend.auth.model;

public record AuthUserView(
    String id,
    String username,
    String displayName,
    String role
) {
}
