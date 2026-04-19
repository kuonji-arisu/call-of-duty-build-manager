package io.github.kuonjiarisu.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
    @NotBlank
    @Size(min = 32)
    String jwtSecret,

    @Min(1)
    long accessTokenTtlMinutes,

    @Min(1)
    long captchaTtlMinutes
) {
}
