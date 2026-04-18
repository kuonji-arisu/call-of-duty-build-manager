package io.github.kuonjiarisu.backend.model.request;

import java.time.LocalDateTime;

import io.github.kuonjiarisu.backend.model.command.BuildItemSaveCommand;

public record BuildItemSaveRequest(
    String id,
    String slot,
    String attachmentId,
    LocalDateTime createdAt
) {
    public BuildItemSaveCommand toCommand() {
        return new BuildItemSaveCommand(id, slot, attachmentId, createdAt);
    }
}
