package io.github.kuonjiarisu.backend.model;

public record InitialData(
    AppInfo appInfo,
    CatalogData catalog,
    java.util.List<AppSetting> settings
) {
}
