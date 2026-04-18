package io.github.kuonjiarisu.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
    String jwtSecret,
    long accessTokenTtlMinutes,
    long captchaTtlMinutes,
    String defaultAdminUsername,
    String defaultAdminPassword,
    String defaultAdminDisplayName
) {
}
