package io.github.kuonjiarisu.backend.model.command;

import java.util.List;

public record AttachmentSaveCommand(
    String name,
    String subtitle,
    String slot,
    List<String> generations,
    List<String> tags,
    List<AttachmentEffectSaveCommand> effects,
    Integer sortOrder
) {
}
