package io.github.kuonjiarisu.backend.model.request;

import io.github.kuonjiarisu.backend.model.command.BuildItemSaveCommand;
import jakarta.validation.constraints.NotBlank;

public record BuildItemSaveRequest(
    @NotBlank(message = "槽位不能为空")
    String slot,
    @NotBlank(message = "配件不能为空")
    String attachmentId
) {
    public BuildItemSaveCommand toCommand() {
        return new BuildItemSaveCommand(slot, attachmentId);
    }
}
