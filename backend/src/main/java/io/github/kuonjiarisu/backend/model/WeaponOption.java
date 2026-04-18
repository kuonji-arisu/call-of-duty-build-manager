package io.github.kuonjiarisu.backend.model;

import java.util.List;

public record WeaponOption(
    String id,
    String name,
    String weaponType,
    List<String> generations,
    List<String> slots
) {
}
