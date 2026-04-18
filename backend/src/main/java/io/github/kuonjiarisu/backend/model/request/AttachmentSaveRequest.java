package io.github.kuonjiarisu.backend.model.request;

import java.util.List;

import io.github.kuonjiarisu.backend.model.command.AttachmentSaveCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AttachmentSaveRequest(
    @NotBlank(message = "配件名称不能为空")
    String name,
    @NotBlank(message = "配件副标题不能为空")
    String subtitle,
    @NotBlank(message = "配件槽位不能为空")
    String slot,
    @NotEmpty(message = "配件代际至少需要一项")
    List<String> generations,
    @NotNull(message = "配件标签不能为空")
    List<String> tags,
    @NotNull(message = "配件效果不能为空")
    List<@Valid AttachmentEffectSaveRequest> effects,
    @Min(value = 0, message = "排序不能小于 0")
    Integer sortOrder
) {
    public AttachmentSaveCommand toCommand() {
        return new AttachmentSaveCommand(
            name,
            subtitle,
            slot,
            generations,
            tags,
            effects == null ? null : effects.stream().map(AttachmentEffectSaveRequest::toCommand).toList(),
            sortOrder
        );
    }
}
