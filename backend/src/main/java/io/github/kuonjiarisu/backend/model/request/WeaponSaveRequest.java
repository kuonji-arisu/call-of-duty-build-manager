package io.github.kuonjiarisu.backend.model.request;

import java.time.LocalDateTime;
import java.util.List;

import io.github.kuonjiarisu.backend.model.command.WeaponSaveCommand;

public record WeaponSaveRequest(
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
    public WeaponSaveCommand toCommand() {
        return new WeaponSaveCommand(
            id,
            name,
            weaponType,
            tags,
            generations,
            slots,
            sortOrder,
            isFavorite,
            createdAt,
            updatedAt
        );
    }
}
