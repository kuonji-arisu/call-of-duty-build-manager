package io.github.kuonjiarisu.backend.auth.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.cors")
public record CorsProperties(
    List<String> allowedOrigins
) {
}
