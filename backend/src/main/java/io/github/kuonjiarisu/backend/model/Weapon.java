package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;
import java.util.List;

public record Weapon(
    String id,
    String name,
    String weaponType,
    List<String> tags,
    List<String> generations,
    List<String> slots,
    Integer sortOrder,
    Boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
