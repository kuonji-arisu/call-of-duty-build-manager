package io.github.kuonjiarisu.backend.model;

import java.util.List;

public record AttachmentBindingCandidate(
    String attachmentId,
    String attachmentName,
    String slot,
    List<String> generations,
    List<String> tags,
    Integer sortOrder,
    Boolean bound
) {
}
