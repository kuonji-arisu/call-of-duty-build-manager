package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record Build(
    String id,
    String weaponId,
    String name,
    String generation,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
