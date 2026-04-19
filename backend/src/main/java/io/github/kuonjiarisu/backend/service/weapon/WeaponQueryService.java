package io.github.kuonjiarisu.backend.service.weapon;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.mapper.WeaponMapper;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.WeaponOption;
import io.github.kuonjiarisu.backend.model.WeaponRow;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.OwnedStringValueSupport;
import io.github.kuonjiarisu.backend.support.PageSupport;

@Service
public class WeaponQueryService {

    private final WeaponMapper weaponMapper;
    private final WeaponHydrator weaponHydrator;

    public WeaponQueryService(WeaponMapper weaponMapper, WeaponHydrator weaponHydrator) {
        this.weaponMapper = weaponMapper;
        this.weaponHydrator = weaponHydrator;
    }

    public List<Weapon> listAll() {
        return weaponHydrator.hydrateAll(weaponMapper.findAllRows());
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

        return new PageResult<>(weaponHydrator.hydrate(rows), total, normalizedPage, normalizedPageSize);
    }

    public Weapon findById(String id) {
        var weaponId = DomainSupport.requireText(id, "武器 ID");
        var row = weaponMapper.findRowById(weaponId);
        if (row == null) {
            throw new IllegalArgumentException("武器不存在");
        }
        return weaponHydrator.hydrate(List.of(row)).getFirst();
    }

    public List<WeaponOption> listOptions() {
        var rows = weaponMapper.findAllRows();
        if (rows.isEmpty()) {
            return List.of();
        }

        return toOptions(rows);
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
        return weaponHydrator.hydrate(weaponMapper.findRowsByIds(normalizedIds)).stream()
            .collect(Collectors.toMap(Weapon::id, weapon -> weapon));
    }

    private List<WeaponOption> toOptions(List<WeaponRow> rows) {
        var ids = rows.stream().map(WeaponRow::id).toList();
        var generationsByWeaponId = OwnedStringValueSupport.groupValues(weaponMapper.findGenerationsByWeaponIds(ids));
        var slotsByWeaponId = OwnedStringValueSupport.groupValues(weaponMapper.findSlotsByWeaponIds(ids));
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
}
