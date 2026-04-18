package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record BuildItem(
    String id,
    String buildId,
    String slot,
    String attachmentId,
    LocalDateTime createdAt
) {
}
