package io.github.kuonjiarisu.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kuonjiarisu.backend.mapper.WeaponMapper;
import io.github.kuonjiarisu.backend.model.OwnedStringValue;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.WeaponRow;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.WeaponOption;
import io.github.kuonjiarisu.backend.model.command.WeaponSaveCommand;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.JsonStringListCodec;
import io.github.kuonjiarisu.backend.support.PageSupport;

@Service
public class WeaponService {

    private static final Logger log = LoggerFactory.getLogger(WeaponService.class);

    private final WeaponMapper weaponMapper;
    private final ReferenceGuardService referenceGuardService;
    private final JsonStringListCodec stringListCodec;

    public WeaponService(
        WeaponMapper weaponMapper,
        ReferenceGuardService referenceGuardService,
        JsonStringListCodec stringListCodec
    ) {
        this.weaponMapper = weaponMapper;
        this.referenceGuardService = referenceGuardService;
        this.stringListCodec = stringListCodec;
    }

    public List<Weapon> listAll() {
        var generationsByWeaponId = groupValues(weaponMapper.findAllGenerations());
        var slotsByWeaponId = groupValues(weaponMapper.findAllSlots());

        return toWeapons(weaponMapper.findAllRows(), generationsByWeaponId, slotsByWeaponId);
    }

    public PageResult<Weapon> listPage(
        Integer page,
        Integer pageSize,
        String keyword,
        String weaponType,
        String generation,
        Boolean favorite
    ) {
        var normalizedPage = PageSupport.normalizePage(page);
        var normalizedPageSize = PageSupport.normalizePageSize(pageSize);
        var normalizedKeyword = PageSupport.normalizeText(keyword);
        var normalizedWeaponType = PageSupport.normalizeText(weaponType);
        var normalizedGeneration = PageSupport.normalizeText(generation);
        var total = weaponMapper.countRows(normalizedKeyword, normalizedWeaponType, normalizedGeneration, favorite);
        var rows = weaponMapper.findRows(
            normalizedKeyword,
            normalizedWeaponType,
            normalizedGeneration,
            favorite,
            normalizedPageSize,
            PageSupport.offset(normalizedPage, normalizedPageSize)
        );

        return new PageResult<>(hydrateRows(rows), total, normalizedPage, normalizedPageSize);
    }

    public Weapon findById(String id) {
        var weaponId = DomainSupport.requireText(id, "武器 ID");
        var row = weaponMapper.findRowById(weaponId);
        if (row == null) {
            throw new IllegalArgumentException("武器不存在");
        }
        return hydrateRows(List.of(row)).getFirst();
    }

    public List<WeaponOption> listOptions() {
        var rows = weaponMapper.findAllRows();
        if (rows.isEmpty()) {
            return List.of();
        }

        var ids = rows.stream().map(WeaponRow::id).toList();
        var generationsByWeaponId = groupValues(weaponMapper.findGenerationsByWeaponIds(ids));
        var slotsByWeaponId = groupValues(weaponMapper.findSlotsByWeaponIds(
            ids
        ));
        return rows.stream()
            .map(row -> new WeaponOption(
                row.id(),
                row.name(),
                row.weaponType(),
                generationsByWeaponId.getOrDefault(row.id(), List.of()),
                slotsByWeaponId.getOrDefault(row.id(), List.of())
            ))
            .toList();
    }

    public PageResult<WeaponOption> searchOptions(Integer page, Integer pageSize, String keyword) {
        var result = listPage(page, pageSize, keyword, null, null, null);
        return new PageResult<>(
            result.items().stream()
                .map(weapon -> new WeaponOption(
                    weapon.id(),
                    weapon.name(),
                    weapon.weaponType(),
                    weapon.generations(),
                    weapon.slots()
                ))
                .toList(),
            result.total(),
            result.page(),
            result.pageSize()
        );
    }

    public Map<String, Weapon> findByIds(List<String> ids) {
        var normalizedIds = ids == null ? List.<String>of() : ids.stream().distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }
        return hydrateRows(weaponMapper.findRowsByIds(normalizedIds)).stream()
            .collect(Collectors.toMap(Weapon::id, weapon -> weapon));
    }

    @Transactional
    public Weapon save(WeaponSaveCommand command) {
        var normalized = new Weapon(
            DomainSupport.keepOrGenerateId(command.id(), "weapon"),
            DomainSupport.requireText(command.name(), "武器名称"),
            DomainSupport.requireText(command.weaponType(), "武器分类"),
            DomainSupport.normalizeList(command.tags()),
            DomainSupport.requireList(command.generations(), "武器代际"),
            DomainSupport.requireList(command.slots(), "武器槽位"),
            command.sortOrder() == null ? 0 : command.sortOrder(),
            Boolean.TRUE.equals(command.isFavorite()),
            DomainSupport.keepOrNow(command.createdAt()),
            LocalDateTime.now()
        );
        var existed = weaponMapper.countById(normalized.id()) > 0;
        validateReferenceSafeUpdate(normalized);
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

    private Map<String, List<String>> groupValues(List<OwnedStringValue> rows) {
        return rows.stream()
            .collect(Collectors.groupingBy(
                OwnedStringValue::ownerId,
                Collectors.mapping(OwnedStringValue::value, Collectors.toList())
            ));
    }

    private List<Weapon> hydrateRows(List<WeaponRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        var ids = rows.stream().map(WeaponRow::id).toList();
        var generationsByWeaponId = groupValues(weaponMapper.findGenerationsByWeaponIds(ids));
        var slotsByWeaponId = groupValues(weaponMapper.findSlotsByWeaponIds(ids));
        return toWeapons(rows, generationsByWeaponId, slotsByWeaponId);
    }

    private List<Weapon> toWeapons(
        List<WeaponRow> rows,
        Map<String, List<String>> generationsByWeaponId,
        Map<String, List<String>> slotsByWeaponId
    ) {
        return rows.stream()
            .map(row -> new Weapon(
                row.id(),
                row.name(),
                row.weaponType(),
                stringListCodec.parse(row.tagsJson()),
                generationsByWeaponId.getOrDefault(row.id(), List.of()),
                slotsByWeaponId.getOrDefault(row.id(), List.of()),
                row.sortOrder(),
                row.isFavorite(),
                row.createdAt(),
                row.updatedAt()
            ))
            .toList();
    }

    private void validateReferenceSafeUpdate(Weapon normalized) {
        var current = listAll().stream()
            .filter(weapon -> weapon.id().equals(normalized.id()))
            .findFirst();
        if (current.isEmpty()) {
            return;
        }

        validateGenerationUpdate(current.get(), normalized);
        validateSlotUpdate(current.get(), normalized);
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
