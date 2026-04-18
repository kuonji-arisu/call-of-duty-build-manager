package io.github.kuonjiarisu.backend.model.request;

import java.time.LocalDateTime;
import java.util.List;

import io.github.kuonjiarisu.backend.model.command.BuildSaveCommand;

public record BuildSaveRequest(
    String id,
    String weaponId,
    String name,
    List<String> generations,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<BuildItemSaveRequest> items
) {
    public BuildSaveCommand toCommand() {
        return new BuildSaveCommand(
            id,
            weaponId,
            name,
            generations,
            notes,
            sortOrder,
            isFavorite,
            createdAt,
            updatedAt,
            items == null ? List.of() : items.stream()
                .map(BuildItemSaveRequest::toCommand)
                .toList()
        );
    }
}
