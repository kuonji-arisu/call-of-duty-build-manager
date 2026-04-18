package io.github.kuonjiarisu.backend.model.command;

public record BuildItemSaveCommand(
    String slot,
    String attachmentId
) {
}
