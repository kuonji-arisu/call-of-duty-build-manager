package io.github.kuonjiarisu.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.auth.config.AuthProperties;
import io.github.kuonjiarisu.backend.auth.model.JwtUserClaims;
import io.github.kuonjiarisu.backend.auth.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    private final AuthProperties authProperties;

    public JwtTokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public String generateAccessToken(UserAccount user) {
        var now = Instant.now();
        var expiresAt = now.plus(accessTokenTtl());

        return Jwts.builder()
            .subject(user.id())
            .claim("username", user.username())
            .claim("role", user.role())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public JwtUserClaims parse(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return new JwtUserClaims(
            claims.getSubject(),
            claims.get("username", String.class),
            claims.get("role", String.class)
        );
    }

    public long accessTokenTtlSeconds() {
        return accessTokenTtl().toSeconds();
    }

    private Duration accessTokenTtl() {
        return Duration.ofMinutes(authProperties.accessTokenTtlMinutes());
    }
}
