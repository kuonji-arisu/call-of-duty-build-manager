package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record AttachmentEffectDefinition(
    String id,
    String label,
    Integer sortOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
