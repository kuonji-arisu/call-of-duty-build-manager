package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record AttachmentRow(
    String id,
    String name,
    String subtitle,
    String slot,
    String tagsJson,
    Integer sortOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
