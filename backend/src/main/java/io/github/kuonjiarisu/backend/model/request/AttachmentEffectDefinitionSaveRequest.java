package io.github.kuonjiarisu.backend.model.request;

import io.github.kuonjiarisu.backend.model.command.AttachmentEffectDefinitionSaveCommand;

public record AttachmentEffectDefinitionSaveRequest(
    String label,
    Integer sortOrder
) {
    public AttachmentEffectDefinitionSaveCommand toCommand() {
        return new AttachmentEffectDefinitionSaveCommand(label, sortOrder);
    }
}
