package io.github.kuonjiarisu.backend.model;

import java.util.List;

public record BuildDetail(
    Build build,
    Weapon weapon,
    List<BuildDetailItem> items
) {
}
