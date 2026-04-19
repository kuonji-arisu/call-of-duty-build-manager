package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record BuildSummary(
    String id,
    String weaponId,
    String weaponName,
    String name,
    String generation,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    int itemCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
