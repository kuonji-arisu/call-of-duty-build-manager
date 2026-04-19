package io.github.kuonjiarisu.backend.service.build;

import org.springframework.stereotype.Component;

import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildRow;
import io.github.kuonjiarisu.backend.model.BuildSummary;
import io.github.kuonjiarisu.backend.model.Weapon;

@Component
public class BuildAssembler {

    public Build toBuild(BuildRow row) {
        return new Build(
            row.id(),
            row.weaponId(),
            row.name(),
            row.generation(),
            row.notes(),
            row.sortOrder(),
            row.isFavorite(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    public BuildSummary toSummary(
        BuildRow row,
        java.util.Map<String, Integer> itemCountsByBuildId,
        Weapon weapon
    ) {
        return new BuildSummary(
            row.id(),
            row.weaponId(),
            weapon == null ? "未知武器" : weapon.name(),
            row.name(),
            row.generation(),
            row.notes(),
            row.sortOrder(),
            row.isFavorite(),
            itemCountsByBuildId.getOrDefault(row.id(), 0),
            row.createdAt(),
            row.updatedAt()
        );
    }
}
