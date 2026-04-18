package io.github.kuonjiarisu.backend.model.request;

import io.github.kuonjiarisu.backend.model.command.AttachmentEffectDefinitionSaveCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AttachmentEffectDefinitionSaveRequest(
    @NotBlank(message = "属性词条名称不能为空")
    String label,
    @Min(value = 0, message = "排序不能小于 0")
    Integer sortOrder
) {
    public AttachmentEffectDefinitionSaveCommand toCommand() {
        return new AttachmentEffectDefinitionSaveCommand(label, sortOrder);
    }
}
