package io.github.kuonjiarisu.backend.support;

import java.util.List;
import java.util.UUID;

import org.springframework.util.StringUtils;

public final class DomainSupport {

    private DomainSupport() {
    }

    public static String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }

        return value.trim();
    }

    public static List<String> requireList(List<String> values, String fieldName) {
        var normalized = normalizeList(values);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 至少需要一项");
        }
        return normalized;
    }

    public static List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .toList();
    }

    public static String keepOrGenerateId(String id, String prefix) {
        return StringUtils.hasText(id) ? id.trim() : UUID.randomUUID().toString();
    }

}
