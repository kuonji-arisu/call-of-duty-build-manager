package io.github.kuonjiarisu.backend.model.command;

import java.time.LocalDateTime;
import java.util.List;

public record BuildSaveCommand(
    String id,
    String weaponId,
    String name,
    List<String> generations,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<BuildItemSaveCommand> items
) {
}
