package io.github.kuonjiarisu.backend.service.weapon;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kuonjiarisu.backend.mapper.WeaponMapper;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.WeaponRow;
import io.github.kuonjiarisu.backend.model.command.WeaponSaveCommand;
import io.github.kuonjiarisu.backend.service.CatalogService;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.JsonStringListCodec;

@Service
public class WeaponCommandService {

    private static final Logger log = LoggerFactory.getLogger(WeaponCommandService.class);

    private final WeaponMapper weaponMapper;
    private final WeaponQueryService weaponQueryService;
    private final ReferenceGuardService referenceGuardService;
    private final JsonStringListCodec stringListCodec;
    private final CatalogService catalogService;

    public WeaponCommandService(
        WeaponMapper weaponMapper,
        WeaponQueryService weaponQueryService,
        ReferenceGuardService referenceGuardService,
        JsonStringListCodec stringListCodec,
        CatalogService catalogService
    ) {
        this.weaponMapper = weaponMapper;
        this.weaponQueryService = weaponQueryService;
        this.referenceGuardService = referenceGuardService;
        this.stringListCodec = stringListCodec;
        this.catalogService = catalogService;
    }

    @Transactional
    public Weapon create(WeaponSaveCommand command) {
        return save(null, command, null);
    }

    @Transactional
    public Weapon update(String id, WeaponSaveCommand command) {
        var weaponId = DomainSupport.requireText(id, "武器 ID");
        var current = weaponQueryService.findById(weaponId);
        return save(weaponId, command, current);
    }

    private Weapon save(String requestedId, WeaponSaveCommand command, Weapon current) {
        var id = DomainSupport.keepOrGenerateId(requestedId, "weapon");
        var currentRow = current == null ? null : weaponMapper.findRowById(id);
        var now = LocalDateTime.now();
        var normalized = new Weapon(
            id,
            DomainSupport.requireText(command.name(), "武器名称"),
            DomainSupport.requireText(command.weaponType(), "武器分类"),
            DomainSupport.normalizeList(command.tags()),
            catalogService.requireGenerations(command.generations(), "武器代际"),
            DomainSupport.requireList(command.slots(), "武器槽位"),
            command.sortOrder() == null ? 0 : command.sortOrder(),
            Boolean.TRUE.equals(command.isFavorite()),
            currentRow == null ? now : currentRow.createdAt(),
            now
        );
        var existed = current != null;
        validateReferenceSafeUpdate(current, normalized);
        weaponMapper.upsertRow(new WeaponRow(
            normalized.id(),
            normalized.name(),
            normalized.weaponType(),
            stringListCodec.serialize(normalized.tags()),
            normalized.sortOrder(),
            normalized.isFavorite(),
            normalized.createdAt(),
            normalized.updatedAt()
        ));
        weaponMapper.deleteGenerationsByWeaponId(normalized.id());
        weaponMapper.deleteSlotsByWeaponId(normalized.id());
        if (!normalized.generations().isEmpty()) {
            weaponMapper.insertGenerations(normalized.id(), normalized.generations());
        }
        if (!normalized.slots().isEmpty()) {
            weaponMapper.insertSlots(normalized.id(), normalized.slots());
        }
        log.info(
            "Saved weapon: weaponId={} operation={} generationCount={} slotCount={}",
            normalized.id(),
            existed ? "update" : "create",
            normalized.generations().size(),
            normalized.slots().size()
        );
        return normalized;
    }

    @Transactional
    public void delete(String id) {
        var weaponId = DomainSupport.requireText(id, "武器 ID");
        if (weaponMapper.countById(weaponId) == 0) {
            throw new IllegalArgumentException("武器不存在");
        }
        referenceGuardService.ensureWeaponCanBeDeleted(weaponId);

        weaponMapper.deleteGenerationsByWeaponId(weaponId);
        weaponMapper.deleteSlotsByWeaponId(weaponId);
        weaponMapper.deleteById(weaponId);
        log.info("Deleted weapon: weaponId={}", weaponId);
    }

    private void validateReferenceSafeUpdate(Weapon current, Weapon normalized) {
        if (current == null) {
            return;
        }

        validateGenerationUpdate(current, normalized);
        validateSlotUpdate(current, normalized);
    }

    // 代际是武器、配件绑定和推荐配装的共同过滤维度；删除代际必须先确认不会留下不可读配装。
    private void validateGenerationUpdate(Weapon current, Weapon normalized) {
        var nextGenerations = new HashSet<>(normalized.generations());
        var removedGenerations = current.generations().stream()
            .filter(generation -> !nextGenerations.contains(generation))
            .toList();
        if (removedGenerations.isEmpty()) {
            return;
        }

        referenceGuardService.ensureWeaponGenerationsCanChange(
            normalized.id(),
            removedGenerations,
            normalized.generations()
        );
    }

    // 槽位一旦被配装或绑定使用，就不能被“顺手清理”，否则读端只能隐藏坏数据。
    private void validateSlotUpdate(Weapon current, Weapon normalized) {
        var nextSlots = new HashSet<>(normalized.slots());
        var removedSlots = current.slots().stream()
            .filter(slot -> !nextSlots.contains(slot))
            .toList();
        if (removedSlots.isEmpty()) {
            return;
        }

        referenceGuardService.ensureWeaponSlotsCanChange(normalized.id(), removedSlots);
    }
}
