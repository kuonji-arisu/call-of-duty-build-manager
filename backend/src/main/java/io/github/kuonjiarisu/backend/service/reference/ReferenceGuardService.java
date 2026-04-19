package io.github.kuonjiarisu.backend.service.reference;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.mapper.AttachmentEffectDefinitionMapper;
import io.github.kuonjiarisu.backend.mapper.AttachmentMapper;
import io.github.kuonjiarisu.backend.mapper.WeaponMapper;
import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.Weapon;

@Service
public class ReferenceGuardService {

    private static final Logger log = LoggerFactory.getLogger(ReferenceGuardService.class);

    private final WeaponMapper weaponMapper;
    private final AttachmentMapper attachmentMapper;
    private final AttachmentEffectDefinitionMapper attachmentEffectDefinitionMapper;

    public ReferenceGuardService(
        WeaponMapper weaponMapper,
        AttachmentMapper attachmentMapper,
        AttachmentEffectDefinitionMapper attachmentEffectDefinitionMapper
    ) {
        this.weaponMapper = weaponMapper;
        this.attachmentMapper = attachmentMapper;
        this.attachmentEffectDefinitionMapper = attachmentEffectDefinitionMapper;
    }

    public void ensureWeaponCanBeDeleted(String weaponId) {
        var buildCount = weaponMapper.countBuildsByWeaponId(weaponId);
        if (buildCount > 0) {
            log.warn("Reference guard rejected weapon deletion: weaponId={} buildCount={}", weaponId, buildCount);
            throw new IllegalArgumentException("该武器仍被配装引用，不能删除");
        }
        var bindingCount = weaponMapper.countAttachmentBindingsByWeaponId(weaponId);
        if (bindingCount > 0) {
            log.warn("Reference guard rejected weapon deletion: weaponId={} bindingCount={}", weaponId, bindingCount);
            throw new IllegalArgumentException("该武器仍有配件绑定，不能删除");
        }
    }

    public void ensureWeaponSlotsCanChange(String weaponId, List<String> removedSlots) {
        if (removedSlots.isEmpty()) {
            return;
        }
        var usedItemCount = weaponMapper.countBuildItemsByWeaponIdAndSlots(weaponId, removedSlots);
        if (usedItemCount > 0) {
            log.warn(
                "Reference guard rejected weapon slot change: weaponId={} removedSlots={} buildItemCount={}",
                weaponId,
                removedSlots,
                usedItemCount
            );
            throw new IllegalArgumentException("被移除的槽位仍被配装使用，不能保存武器");
        }
        var bindingCount = weaponMapper.countAttachmentBindingsByWeaponIdAndSlots(weaponId, removedSlots);
        if (bindingCount > 0) {
            log.warn(
                "Reference guard rejected weapon slot change: weaponId={} removedSlots={} bindingCount={}",
                weaponId,
                removedSlots,
                bindingCount
            );
            throw new IllegalArgumentException("被移除的槽位仍有配件绑定，不能保存武器");
        }
    }

    public void ensureWeaponGenerationsCanChange(String weaponId, List<String> removedGenerations, List<String> nextGenerations) {
        if (removedGenerations.isEmpty()) {
            return;
        }
        var buildCount = weaponMapper.countBuildsByWeaponIdAndGenerations(weaponId, removedGenerations);
        if (buildCount > 0) {
            log.warn(
                "Reference guard rejected weapon generation change: weaponId={} removedGenerations={} buildCount={}",
                weaponId,
                removedGenerations,
                buildCount
            );
            throw new IllegalArgumentException("被移除的代际仍被配装使用，不能保存武器");
        }
        var invalidBindingCount = weaponMapper.countInvalidAttachmentBindingsByWeaponIdAndGenerations(weaponId, nextGenerations);
        if (invalidBindingCount > 0) {
            log.warn(
                "Reference guard rejected weapon generation change: weaponId={} removedGenerations={} invalidBindingCount={}",
                weaponId,
                removedGenerations,
                invalidBindingCount
            );
            throw new IllegalArgumentException("被移除的代际仍有配件绑定，不能保存武器");
        }
    }

    public void ensureAttachmentCanBeDeleted(String attachmentId) {
        var buildItemCount = attachmentMapper.countBuildItemsByAttachmentId(attachmentId);
        if (buildItemCount > 0) {
            log.warn(
                "Reference guard rejected attachment deletion: attachmentId={} buildItemCount={}",
                attachmentId,
                buildItemCount
            );
            throw new IllegalArgumentException("该配件仍被配装使用，不能删除");
        }
    }

    public void ensureAttachmentBindingsCanBeRemoved(String weaponId, List<String> attachmentIds) {
        var buildItemCount = attachmentMapper.countBuildItemsByWeaponIdAndAttachmentIds(weaponId, attachmentIds);
        if (buildItemCount > 0) {
            log.warn(
                "Reference guard rejected binding removal: weaponId={} attachmentIds={} buildItemCount={}",
                weaponId,
                attachmentIds,
                buildItemCount
            );
            throw new IllegalArgumentException("选中的绑定仍被推荐配装使用，不能移除");
        }
    }

    public void ensureEffectDefinitionCanBeDeleted(String definitionId) {
        var effectCount = attachmentEffectDefinitionMapper.countEffectsByDefinitionId(definitionId);
        if (effectCount > 0) {
            log.warn(
                "Reference guard rejected effect definition deletion: definitionId={} effectCount={}",
                definitionId,
                effectCount
            );
            throw new IllegalArgumentException("该属性词条仍被配件效果引用，不能删除");
        }
    }

    // 读取端选择隐藏坏引用，而不是级联删除或自动修复，避免浏览接口在脏数据下放大破坏面。
    public boolean isReadableBuildItem(
        BuildItem item,
        Build build,
        Weapon weapon,
        Attachment attachment
    ) {
        if (build == null || weapon == null || attachment == null) {
            return false;
        }

        return weapon.slots().contains(item.slot())
            && attachment.slot().equals(item.slot())
            && attachment.weaponIds().contains(weapon.id())
            && intersects(attachment.generations(), weapon.generations())
            && attachment.generations().contains(build.generation());
    }

    public Weapon requireReadableBuildWeapon(Weapon weapon) {
        if (weapon != null) {
            return weapon;
        }
        log.warn("Reference guard rejected build read: weapon is missing");
        throw new IllegalArgumentException("配装所属武器不存在");
    }

    // 当前项目不依赖数据库外键，缺失武器的配装在读取列表时应被隐藏，详情页再显式拒绝。
    public boolean shouldHideMissingWeapon() {
        return true;
    }

    public boolean intersects(List<String> left, List<String> right) {
        return left.stream().anyMatch(right::contains);
    }
}
