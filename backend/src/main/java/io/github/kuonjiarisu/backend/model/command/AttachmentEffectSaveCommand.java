package io.github.kuonjiarisu.backend.model.command;

public record AttachmentEffectSaveCommand(
    String definitionId,
    String effectType,
    Integer level
) {
}
