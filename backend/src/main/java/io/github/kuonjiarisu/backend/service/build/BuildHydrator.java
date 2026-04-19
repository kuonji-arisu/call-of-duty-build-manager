package io.github.kuonjiarisu.backend.service.build;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.kuonjiarisu.backend.mapper.BuildMapper;
import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.BuildRow;
import io.github.kuonjiarisu.backend.model.BuildSummary;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.service.weapon.WeaponQueryService;
import io.github.kuonjiarisu.backend.support.OwnedStringValueSupport;

@Component
public class BuildHydrator {

    private static final Logger log = LoggerFactory.getLogger(BuildHydrator.class);

    private final BuildMapper buildMapper;
    private final WeaponQueryService weaponQueryService;
    private final BuildAssembler buildAssembler;
    private final ReferenceGuardService referenceGuardService;

    public BuildHydrator(
        BuildMapper buildMapper,
        WeaponQueryService weaponQueryService,
        BuildAssembler buildAssembler,
        ReferenceGuardService referenceGuardService
    ) {
        this.buildMapper = buildMapper;
        this.weaponQueryService = weaponQueryService;
        this.buildAssembler = buildAssembler;
        this.referenceGuardService = referenceGuardService;
    }

    public List<Build> toBuilds(List<BuildRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        var existingWeaponIds = weaponQueryService.findByIds(
            rows.stream().map(BuildRow::weaponId).distinct().toList()
        ).keySet();
        var generationsByBuildId = OwnedStringValueSupport.groupValues(buildMapper.findGenerationsByBuildIds(
            rows.stream().map(BuildRow::id).toList()
        ));
        warnMissingWeapons("build list", rows, existingWeaponIds);
        return rows.stream()
            .filter(row -> existingWeaponIds.contains(row.weaponId()) || !referenceGuardService.shouldHideMissingWeapon())
            .map(row -> buildAssembler.toBuild(row, generationsByBuildId))
            .toList();
    }

    public List<BuildSummary> toSummaries(List<BuildRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        var ids = rows.stream().map(BuildRow::id).toList();
        var generationsByBuildId = OwnedStringValueSupport.groupValues(buildMapper.findGenerationsByBuildIds(ids));
        var itemCountsByBuildId = buildMapper.findBuildItemsByBuildIds(ids).stream()
            .collect(Collectors.groupingBy(BuildItem::buildId, Collectors.collectingAndThen(
                Collectors.counting(),
                Long::intValue
            )));
        var weaponsById = weaponQueryService.findByIds(
            rows.stream()
                .map(BuildRow::weaponId)
                .distinct()
                .toList()
        );

        warnMissingWeapons("build summary", rows, weaponsById.keySet());
        return rows.stream()
            .filter(row -> weaponsById.containsKey(row.weaponId()) || !referenceGuardService.shouldHideMissingWeapon())
            .map(row -> buildAssembler.toSummary(
                row,
                generationsByBuildId,
                itemCountsByBuildId,
                weaponsById.get(row.weaponId())
            ))
            .toList();
    }

    private void warnMissingWeapons(String scope, List<BuildRow> rows, Set<String> existingWeaponIds) {
        var invalidRows = rows.stream()
            .filter(row -> !existingWeaponIds.contains(row.weaponId()))
            .toList();
        if (invalidRows.isEmpty() || !referenceGuardService.shouldHideMissingWeapon()) {
            return;
        }

        log.warn(
            "Skipped builds with missing weapon during {} hydration: buildCount={} sampleBuildIds={}",
            scope,
            invalidRows.size(),
            invalidRows.stream().map(BuildRow::id).limit(5).toList()
        );
    }
}
