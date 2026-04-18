package io.github.kuonjiarisu.backend.model.request;

import java.util.List;

import io.github.kuonjiarisu.backend.model.command.WeaponSaveCommand;

public record WeaponSaveRequest(
    String name,
    String weaponType,
    List<String> tags,
    List<String> generations,
    List<String> slots,
    Integer sortOrder,
    Boolean isFavorite
) {
    public WeaponSaveCommand toCommand() {
        return new WeaponSaveCommand(
            name,
            weaponType,
            tags,
            generations,
            slots,
            sortOrder,
            isFavorite
        );
    }
}
