package io.github.kuonjiarisu.backend.model.request;

import java.util.List;

import io.github.kuonjiarisu.backend.model.AttachmentEffect;
import io.github.kuonjiarisu.backend.model.command.AttachmentSaveCommand;

public record AttachmentSaveRequest(
    String name,
    String subtitle,
    String slot,
    List<String> generations,
    List<String> tags,
    List<AttachmentEffect> effects,
    Integer sortOrder
) {
    public AttachmentSaveCommand toCommand() {
        return new AttachmentSaveCommand(
            name,
            subtitle,
            slot,
            generations,
            tags,
            effects,
            sortOrder
        );
    }
}
