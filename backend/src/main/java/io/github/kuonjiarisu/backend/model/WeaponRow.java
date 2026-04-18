package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record WeaponRow(
    String id,
    String name,
    String weaponType,
    String tagsJson,
    Integer sortOrder,
    Boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
