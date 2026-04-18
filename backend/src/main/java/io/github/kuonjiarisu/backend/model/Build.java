package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;
import java.util.List;

public record Build(
    String id,
    String weaponId,
    String name,
    List<String> generations,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
