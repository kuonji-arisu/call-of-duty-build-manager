package io.github.kuonjiarisu.backend.service.weapon;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.kuonjiarisu.backend.mapper.WeaponMapper;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.WeaponRow;
import io.github.kuonjiarisu.backend.support.JsonStringListCodec;
import io.github.kuonjiarisu.backend.support.OwnedStringValueSupport;

@Component
public class WeaponHydrator {

    private final WeaponMapper weaponMapper;
    private final JsonStringListCodec stringListCodec;

    public WeaponHydrator(WeaponMapper weaponMapper, JsonStringListCodec stringListCodec) {
        this.weaponMapper = weaponMapper;
        this.stringListCodec = stringListCodec;
    }

    public List<Weapon> hydrateAll(List<WeaponRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        var generationsByWeaponId = OwnedStringValueSupport.groupValues(weaponMapper.findAllGenerations());
        var slotsByWeaponId = OwnedStringValueSupport.groupValues(weaponMapper.findAllSlots());
        return toWeapons(rows, generationsByWeaponId, slotsByWeaponId);
    }

    public List<Weapon> hydrate(List<WeaponRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        var ids = rows.stream().map(WeaponRow::id).toList();
        var generationsByWeaponId = OwnedStringValueSupport.groupValues(weaponMapper.findGenerationsByWeaponIds(ids));
        var slotsByWeaponId = OwnedStringValueSupport.groupValues(weaponMapper.findSlotsByWeaponIds(ids));
        return toWeapons(rows, generationsByWeaponId, slotsByWeaponId);
    }

    private Weapon toWeapon(
        WeaponRow row,
        Map<String, List<String>> generationsByWeaponId,
        Map<String, List<String>> slotsByWeaponId
    ) {
        return new Weapon(
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
        );
    }

    private List<Weapon> toWeapons(
        List<WeaponRow> rows,
        Map<String, List<String>> generationsByWeaponId,
        Map<String, List<String>> slotsByWeaponId
    ) {
        return rows.stream()
            .map(row -> toWeapon(row, generationsByWeaponId, slotsByWeaponId))
            .toList();
    }
}
