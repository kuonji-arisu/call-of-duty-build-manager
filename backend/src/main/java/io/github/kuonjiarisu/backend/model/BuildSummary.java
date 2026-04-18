package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;
import java.util.List;

public record BuildSummary(
    String id,
    String weaponId,
    String weaponName,
    String name,
    List<String> generations,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    int itemCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
