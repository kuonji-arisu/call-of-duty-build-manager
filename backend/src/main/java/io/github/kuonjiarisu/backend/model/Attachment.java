package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;
import java.util.List;

public record Attachment(
    String id,
    String name,
    String subtitle,
    String slot,
    List<String> weaponIds,
    List<String> generations,
    List<String> tags,
    List<AttachmentEffect> effects,
    Integer sortOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
