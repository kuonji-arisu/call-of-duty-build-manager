package io.github.kuonjiarisu.backend.support;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.kuonjiarisu.backend.model.OwnedStringValue;

public final class OwnedStringValueSupport {

    private OwnedStringValueSupport() {
    }

    public static Map<String, List<String>> groupValues(List<OwnedStringValue> rows) {
        return rows.stream()
            .collect(Collectors.groupingBy(
                OwnedStringValue::ownerId,
                Collectors.mapping(OwnedStringValue::value, Collectors.toList())
            ));
    }
}
