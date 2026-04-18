package io.github.kuonjiarisu.backend.model;

public record AttachmentEffect(
    String definitionId,
    String effectType,
    Integer level,
    String label,
    Integer sortOrder
) {
}
