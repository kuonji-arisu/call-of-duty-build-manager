package io.github.kuonjiarisu.backend.model.command;

public record AttachmentEffectDefinitionSaveCommand(
    String label,
    Integer sortOrder
) {
}
