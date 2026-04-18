package io.github.kuonjiarisu.backend.model.request;

import java.time.LocalDateTime;
import java.util.List;

import io.github.kuonjiarisu.backend.model.AttachmentEffect;
import io.github.kuonjiarisu.backend.model.command.AttachmentSaveCommand;

public record AttachmentSaveRequest(
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
    public AttachmentSaveCommand toCommand() {
        return new AttachmentSaveCommand(
            id,
            name,
            subtitle,
            slot,
            generations,
            tags,
            effects,
            sortOrder,
            createdAt,
            updatedAt
        );
    }
}
