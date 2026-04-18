package io.github.kuonjiarisu.backend.model;

public record OwnedStringValue(
    String ownerId,
    String value
) {
}
