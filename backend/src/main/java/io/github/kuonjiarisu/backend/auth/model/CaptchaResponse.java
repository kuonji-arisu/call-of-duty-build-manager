package io.github.kuonjiarisu.backend.auth.model;

public record CaptchaResponse(
    String captchaId,
    String imageData,
    long expiresInSeconds
) {
}
