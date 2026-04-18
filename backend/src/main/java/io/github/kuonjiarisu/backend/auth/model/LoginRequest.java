package io.github.kuonjiarisu.backend.auth.model;

public record LoginRequest(
    String username,
    String password,
    String captchaId,
    String captchaCode
) {
}
