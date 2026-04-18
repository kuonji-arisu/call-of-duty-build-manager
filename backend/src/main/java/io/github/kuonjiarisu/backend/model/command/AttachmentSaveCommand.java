package io.github.kuonjiarisu.backend.model.command;

import java.time.LocalDateTime;
import java.util.List;

import io.github.kuonjiarisu.backend.model.AttachmentEffect;

public record AttachmentSaveCommand(
    String id,
    String name,
    String subtitle,
    String slot,
    List<String> generations,
    List<String> tags,
    List<AttachmentEffect> effects,
    Integer sortOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
