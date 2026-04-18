package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record BuildRow(
    String id,
    String weaponId,
    String name,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
