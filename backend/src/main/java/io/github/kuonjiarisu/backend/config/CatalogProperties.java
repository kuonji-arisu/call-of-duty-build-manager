package io.github.kuonjiarisu.backend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.kuonjiarisu.backend.model.GenerationOption;

@ConfigurationProperties(prefix = "app.catalog")
public record CatalogProperties(
    List<GenerationOption> generations
) {
}
