package io.github.kuonjiarisu.backend.model;

public record AttachmentEffectRow(
    String attachmentId,
    String definitionId,
    String effectType,
    Integer level
) {
}
