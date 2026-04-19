package io.github.kuonjiarisu.backend.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.config.CatalogProperties;
import io.github.kuonjiarisu.backend.model.CatalogData;
import io.github.kuonjiarisu.backend.model.GenerationOption;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class CatalogService {

    private final List<GenerationOption> generations;
    private final Set<String> generationValues;

    public CatalogService(CatalogProperties catalogProperties) {
        this.generations = List.copyOf(catalogProperties.generations() == null ? List.of() : catalogProperties.generations());
        this.generationValues = generations.stream()
            .map(GenerationOption::value)
            .collect(Collectors.toUnmodifiableSet());
    }

    public CatalogData catalogData() {
        return new CatalogData(generations);
    }

    public String requireGeneration(String value, String fieldName) {
        var generation = DomainSupport.requireText(value, fieldName);
        if (!generationValues.contains(generation)) {
            throw new IllegalArgumentException(fieldName + " 不支持: " + generation);
        }
        return generation;
    }

    public List<String> requireGenerations(List<String> values, String fieldName) {
        var normalized = DomainSupport.requireList(values, fieldName);
        var invalidGenerations = normalized.stream()
            .filter(generation -> !generationValues.contains(generation))
            .toList();
        if (!invalidGenerations.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不支持: " + String.join(", ", invalidGenerations));
        }
        return normalized;
    }

    public boolean isGenerationOrAll(String value) {
        return "ALL".equals(value) || generationValues.contains(value);
    }
}
