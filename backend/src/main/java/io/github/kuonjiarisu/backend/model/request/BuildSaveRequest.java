package io.github.kuonjiarisu.backend.model.request;

import java.util.List;

import io.github.kuonjiarisu.backend.model.command.BuildSaveCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BuildSaveRequest(
    @NotBlank(message = "所属武器不能为空")
    String weaponId,
    @NotBlank(message = "配装名称不能为空")
    String name,
    @NotEmpty(message = "配装代际至少需要一项")
    @Size(max = 1, message = "配装只能选择一个代际")
    List<String> generations,
    String notes,
    @Min(value = 0, message = "排序不能小于 0")
    Integer sortOrder,
    @NotNull(message = "收藏状态不能为空")
    Boolean isFavorite,
    @NotNull(message = "配装明细不能为空")
    List<@Valid BuildItemSaveRequest> items
) {
    public BuildSaveCommand toCommand() {
        return new BuildSaveCommand(
            weaponId,
            name,
            generations,
            notes,
            sortOrder,
            isFavorite,
            items == null ? List.of() : items.stream()
                .map(BuildItemSaveRequest::toCommand)
                .toList()
        );
    }
}
