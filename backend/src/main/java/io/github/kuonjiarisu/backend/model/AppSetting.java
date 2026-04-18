package io.github.kuonjiarisu.backend.model;

import java.time.LocalDateTime;

public record AppSetting(String key, String value, LocalDateTime updatedAt) {
}
