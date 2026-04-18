package io.github.kuonjiarisu.backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${APP_CORS_ALLOWED_ORIGINS:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toArray(String[]::new);

        registry.addMapping("/api/**")
            .allowedOrigins(origins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }
}
