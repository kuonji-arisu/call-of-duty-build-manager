package io.github.kuonjiarisu.backend.model.command;

import java.util.List;

public record WeaponSaveCommand(
    String name,
    String weaponType,
    List<String> tags,
    List<String> generations,
    List<String> slots,
    Integer sortOrder,
    Boolean isFavorite
) {
}
