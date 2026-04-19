package io.github.kuonjiarisu.backend.service.attachment;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.mapper.BuildMapper;
import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinition;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.BuildRow;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.command.AttachmentEffectSaveCommand;
import io.github.kuonjiarisu.backend.service.AttachmentEffectDefinitionService;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.service.weapon.WeaponQueryService;

@Service
public class AttachmentValidationService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentValidationService.class);

    private final BuildMapper buildMapper;
    private final WeaponQueryService weaponQueryService;
    private final AttachmentEffectDefinitionService attachmentEffectDefinitionService;
    private final ReferenceGuardService referenceGuardService;

    public AttachmentValidationService(
        BuildMapper buildMapper,
        WeaponQueryService weaponQueryService,
        AttachmentEffectDefinitionService attachmentEffectDefinitionService,
        ReferenceGuardService referenceGuardService
    ) {
        this.buildMapper = buildMapper;
        this.weaponQueryService = weaponQueryService;
        this.attachmentEffectDefinitionService = attachmentEffectDefinitionService;
        this.referenceGuardService = referenceGuardService;
    }

    public void validateExistingBindings(String slot, List<String> generations, List<String> weaponIds) {
        var weaponsById = weaponQueryService.findByIds(weaponIds);

        for (var weaponId : weaponIds) {
            var weapon = weaponsById.get(weaponId);
            if (weapon == null) {
                throw new IllegalArgumentException("绑定武器不存在: " + weaponId);
            }
            if (!weapon.slots().contains(slot)) {
                throw new IllegalArgumentException("绑定武器不包含该槽位: " + weapon.name());
            }
            if (!referenceGuardService.intersects(generations, weapon.generations())) {
                throw new IllegalArgumentException("配件代际与绑定武器不匹配: " + weapon.name());
            }
        }
    }

    public void validateEffectDefinitions(List<AttachmentEffectSaveCommand> effects) {
        if (effects.isEmpty()) {
            return;
        }

        var definitionIds = attachmentEffectDefinitionService.listAll().stream()
            .map(AttachmentEffectDefinition::id)
            .collect(Collectors.toSet());

        for (var effect : effects) {
            if (!definitionIds.contains(effect.definitionId())) {
                throw new IllegalArgumentException("属性词条不存在: " + effect.definitionId());
            }
        }
    }

    public void validateExistingBuildItemUsages(Attachment normalized) {
        var usedItems = buildMapper.findBuildItemsByAttachmentId(normalized.id());
        if (usedItems.isEmpty()) {
            return;
        }

        var buildIds = usedItems.stream()
            .map(BuildItem::buildId)
            .distinct()
            .toList();
        var buildsById = buildMapper.findBuildRowsByIds(buildIds).stream()
            .collect(Collectors.toMap(BuildRow::id, Function.identity()));
        var weaponsById = weaponQueryService.findByIds(
            buildsById.values().stream()
                .map(BuildRow::weaponId)
                .distinct()
                .toList()
        );

        // 这里不是重新计算配件可用性，而是保护已存在的推荐配装不被一次配件编辑改坏。
        for (var item : usedItems) {
            validateExistingBuildItemUsage(normalized, item, buildsById, weaponsById);
        }
    }

    public void validateBindingUpdate(Weapon weapon, Map<String, Attachment> attachmentsById, List<String> attachmentIds) {
        for (var attachmentId : attachmentIds) {
            var attachment = attachmentsById.get(attachmentId);
            if (attachment == null) {
                throw new IllegalArgumentException("部分配件不存在，无法更新绑定");
            }
            if (!weapon.slots().contains(attachment.slot())) {
                throw new IllegalArgumentException("配件槽位不属于该武器: " + attachment.name());
            }
            if (!referenceGuardService.intersects(attachment.generations(), weapon.generations())) {
                throw new IllegalArgumentException("配件代际与武器不匹配: " + attachment.name());
            }
        }
    }

    private void validateExistingBuildItemUsage(
        Attachment normalized,
        BuildItem item,
        Map<String, BuildRow> buildsById,
        Map<String, Weapon> weaponsById
    ) {
        var build = buildsById.get(item.buildId());
        if (build == null) {
            log.warn(
                "Skipped build item usage validation because build is missing: attachmentId={} buildItemId={} buildId={}",
                normalized.id(),
                item.id(),
                item.buildId()
            );
            return;
        }

        var weapon = weaponsById.get(build.weaponId());
        if (weapon == null) {
            log.warn(
                "Skipped build item usage validation because weapon is missing: attachmentId={} buildItemId={} buildId={} weaponId={}",
                normalized.id(),
                item.id(),
                build.id(),
                build.weaponId()
            );
            return;
        }

        // 任一条件失败都说明“编辑后的配件”会让旧配装项不可读，所以必须在写入前拒绝。
        if (!normalized.slot().equals(item.slot())) {
            throw new IllegalArgumentException("该配件仍被配装使用，不能修改槽位");
        }
        if (!normalized.weaponIds().contains(build.weaponId())) {
            throw new IllegalArgumentException("该配件仍被配装使用，不能移除对应武器绑定");
        }
        if (!referenceGuardService.intersects(normalized.generations(), weapon.generations())) {
            throw new IllegalArgumentException("该配件仍被配装使用，不能移除与武器匹配的代际");
        }
        if (!normalized.generations().contains(build.generation())) {
            throw new IllegalArgumentException("该配件仍被配装使用，不能移除与配装匹配的代际");
        }
    }
}
