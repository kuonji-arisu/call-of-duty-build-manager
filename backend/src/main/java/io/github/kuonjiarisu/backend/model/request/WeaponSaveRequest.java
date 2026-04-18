package io.github.kuonjiarisu.backend.model.request;

import java.util.List;

import io.github.kuonjiarisu.backend.model.command.WeaponSaveCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record WeaponSaveRequest(
    @NotBlank(message = "武器名称不能为空")
    String name,
    @NotBlank(message = "武器分类不能为空")
    String weaponType,
    @NotNull(message = "武器标签不能为空")
    List<String> tags,
    @NotEmpty(message = "武器代际至少需要一项")
    List<String> generations,
    @NotEmpty(message = "武器槽位至少需要一项")
    List<String> slots,
    @Min(value = 0, message = "排序不能小于 0")
    Integer sortOrder,
    @NotNull(message = "收藏状态不能为空")
    Boolean isFavorite
) {
    public WeaponSaveCommand toCommand() {
        return new WeaponSaveCommand(
            name,
            weaponType,
            tags,
            generations,
            slots,
            sortOrder,
            isFavorite
        );
    }
}
