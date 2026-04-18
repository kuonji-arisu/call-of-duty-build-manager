package io.github.kuonjiarisu.backend.auth.model;

public record JwtUserClaims(
    String userId,
    String username,
    String role
) {
}
