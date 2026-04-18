package io.github.kuonjiarisu.backend.model.command;

import java.time.LocalDateTime;

public record BuildItemSaveCommand(
    String id,
    String slot,
    String attachmentId,
    LocalDateTime createdAt
) {
}
