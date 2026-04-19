package io.github.kuonjiarisu.backend.model;

import java.util.List;

public record CatalogData(
    List<GenerationOption> generations
) {
}
