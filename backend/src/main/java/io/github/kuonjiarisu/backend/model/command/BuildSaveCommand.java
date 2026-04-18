package io.github.kuonjiarisu.backend.model.command;

import java.util.List;

public record BuildSaveCommand(
    String weaponId,
    String name,
    List<String> generations,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    List<BuildItemSaveCommand> items
) {
}
