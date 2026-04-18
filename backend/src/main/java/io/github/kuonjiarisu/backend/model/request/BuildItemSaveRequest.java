package io.github.kuonjiarisu.backend.model.request;

import io.github.kuonjiarisu.backend.model.command.BuildItemSaveCommand;

public record BuildItemSaveRequest(
    String slot,
    String attachmentId
) {
    public BuildItemSaveCommand toCommand() {
        return new BuildItemSaveCommand(slot, attachmentId);
    }
}
