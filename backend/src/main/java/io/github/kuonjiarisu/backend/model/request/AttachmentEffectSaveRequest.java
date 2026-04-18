package io.github.kuonjiarisu.backend.model.request;

import io.github.kuonjiarisu.backend.model.command.AttachmentEffectSaveCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AttachmentEffectSaveRequest(
    @NotBlank(message = "属性词条不能为空")
    String definitionId,
    @NotBlank(message = "效果类型不能为空")
    String effectType,
    @NotNull(message = "效果等级不能为空")
    @Min(value = 1, message = "效果等级不能小于 1")
    @Max(value = 4, message = "效果等级不能大于 4")
    Integer level
) {
    public AttachmentEffectSaveCommand toCommand() {
        return new AttachmentEffectSaveCommand(definitionId, effectType, level);
    }
}
