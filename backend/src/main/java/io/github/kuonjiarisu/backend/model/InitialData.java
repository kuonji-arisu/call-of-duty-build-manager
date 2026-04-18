package io.github.kuonjiarisu.backend.model;

public record InitialData(
    AppInfo appInfo,
    java.util.List<AppSetting> settings
) {
}
