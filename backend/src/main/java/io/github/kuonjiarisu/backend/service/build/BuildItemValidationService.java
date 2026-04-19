package io.github.kuonjiarisu.backend.service.build;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;

@Service
public class BuildItemValidationService {

    private final ReferenceGuardService referenceGuardService;

    public BuildItemValidationService(ReferenceGuardService referenceGuardService) {
        this.referenceGuardService = referenceGuardService;
    }

    public void validateItems(
        Build build,
        Weapon weapon,
        List<BuildItem> items,
        Map<String, Attachment> attachmentsById
    ) {
        validateBuildGenerations(build, weapon);

        // 推荐配装按“一个槽位一个配件”保存，先在内存中拦截重复槽位，避免覆盖成难排查的半合法数据。
        var usedSlots = new HashSet<String>();

        for (var item : items) {
            if (!usedSlots.add(item.slot())) {
                throw new IllegalArgumentException("配装槽位重复: " + item.slot());
            }
            validateItem(build, weapon, item, attachmentsById);
        }
    }

    private void validateBuildGenerations(Build build, Weapon weapon) {
        if (!weapon.generations().contains(build.generation())) {
            throw new IllegalArgumentException("配装代际不属于所属武器: " + build.generation());
        }
    }

    private void validateItem(
        Build build,
        Weapon weapon,
        BuildItem item,
        Map<String, Attachment> attachmentsById
    ) {
        var attachment = attachmentsById.get(item.attachmentId());
        if (attachment == null) {
            throw new IllegalArgumentException("配件不存在: " + item.attachmentId());
        }

        // 这里同时校验武器槽位、绑定关系和代际交集；任一条件放宽都会让读端只能隐藏坏配装项。
        if (!weapon.slots().contains(item.slot())) {
            throw new IllegalArgumentException("武器不包含该槽位: " + item.slot());
        }

        if (!attachment.slot().equals(item.slot())) {
            throw new IllegalArgumentException("配件槽位与配装槽位不一致: " + attachment.name());
        }

        if (!attachment.weaponIds().contains(weapon.id())) {
            throw new IllegalArgumentException("配件未绑定到该武器: " + attachment.name());
        }

        if (!referenceGuardService.intersects(attachment.generations(), weapon.generations())) {
            throw new IllegalArgumentException("配件代际与武器不匹配: " + attachment.name());
        }

        if (!attachment.generations().contains(build.generation())) {
            throw new IllegalArgumentException("配件代际与配装不匹配: " + attachment.name());
        }
    }
}
