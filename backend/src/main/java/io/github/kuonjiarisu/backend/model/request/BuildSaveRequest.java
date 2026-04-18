package io.github.kuonjiarisu.backend.model.request;

import java.util.List;

import io.github.kuonjiarisu.backend.model.command.BuildSaveCommand;

public record BuildSaveRequest(
    String weaponId,
    String name,
    List<String> generations,
    String notes,
    Integer sortOrder,
    Boolean isFavorite,
    List<BuildItemSaveRequest> items
) {
    public BuildSaveCommand toCommand() {
        return new BuildSaveCommand(
            weaponId,
            name,
            generations,
            notes,
            sortOrder,
            isFavorite,
            items == null ? List.of() : items.stream()
                .map(BuildItemSaveRequest::toCommand)
                .toList()
        );
    }
}
