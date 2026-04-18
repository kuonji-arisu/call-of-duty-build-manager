package io.github.kuonjiarisu.backend.service.attachment;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.mapper.AttachmentMapper;
import io.github.kuonjiarisu.backend.mapper.BuildMapper;
import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentEffect;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinition;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.BuildRow;
import io.github.kuonjiarisu.backend.model.OwnedStringValue;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.service.AttachmentEffectDefinitionService;
import io.github.kuonjiarisu.backend.service.WeaponService;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;

@Service
public class AttachmentValidationService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentValidationService.class);

    private final AttachmentMapper attachmentMapper;
    private final BuildMapper buildMapper;
    private final WeaponService weaponService;
    private final AttachmentEffectDefinitionService attachmentEffectDefinitionService;
    private final AttachmentHydrator attachmentHydrator;
    private final ReferenceGuardService referenceGuardService;

    public AttachmentValidationService(
        AttachmentMapper attachmentMapper,
        BuildMapper buildMapper,
        WeaponService weaponService,
        AttachmentEffectDefinitionService attachmentEffectDefinitionService,
        AttachmentHydrator attachmentHydrator,
        ReferenceGuardService referenceGuardService
    ) {
        this.attachmentMapper = attachmentMapper;
        this.buildMapper = buildMapper;
        this.weaponService = weaponService;
        this.attachmentEffectDefinitionService = attachmentEffectDefinitionService;
        this.attachmentHydrator = attachmentHydrator;
        this.referenceGuardService = referenceGuardService;
    }

    public void validateExistingBindings(String slot, List<String> weaponIds) {
        var weaponsById = weaponService.listAll().stream()
            .collect(Collectors.toMap(Weapon::id, Function.identity()));

        for (var weaponId : weaponIds) {
            var weapon = weaponsById.get(weaponId);
            if (weapon == null) {
                throw new IllegalArgumentException("绑定武器不存在: " + weaponId);
            }
            if (!weapon.slots().contains(slot)) {
                throw new IllegalArgumentException("绑定武器不包含该槽位: " + weapon.name());
            }
        }
    }

    public void validateEffectDefinitions(List<AttachmentEffect> effects) {
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
        var usedItems = buildMapper.findAllBuildItems().stream()
            .filter(item -> normalized.id().equals(item.attachmentId()))
            .toList();
        if (usedItems.isEmpty()) {
            return;
        }

        var buildsById = buildMapper.findAllBuildRows().stream()
            .collect(Collectors.toMap(BuildRow::id, Function.identity()));
        var buildGenerationsById = attachmentHydrator.groupValues(buildMapper.findAllGenerations());
        var weaponsById = weaponService.listAll().stream()
            .collect(Collectors.toMap(Weapon::id, Function.identity()));

        // 这里不是重新计算配件可用性，而是保护已存在的推荐配装不被一次配件编辑改坏。
        for (var item : usedItems) {
            validateExistingBuildItemUsage(normalized, item, buildsById, buildGenerationsById, weaponsById);
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
        Map<String, List<String>> buildGenerationsById,
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
        if (!referenceGuardService.intersects(
            normalized.generations(),
            buildGenerationsById.getOrDefault(build.id(), List.of())
        )) {
            throw new IllegalArgumentException("该配件仍被配装使用，不能移除与配装匹配的代际");
        }
    }
}
