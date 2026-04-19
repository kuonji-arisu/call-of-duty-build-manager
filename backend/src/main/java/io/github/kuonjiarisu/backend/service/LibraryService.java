package io.github.kuonjiarisu.backend.service;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.model.AppInfo;
import io.github.kuonjiarisu.backend.model.InitialData;

@Service
public class LibraryService {

    private final SettingService settingService;
    private final CatalogService catalogService;

    public LibraryService(SettingService settingService, CatalogService catalogService) {
        this.settingService = settingService;
        this.catalogService = catalogService;
    }

    public InitialData loadInitialData() {
        return new InitialData(
            new AppInfo(resolveLanguage()),
            catalogService.catalogData(),
            settingService.listAll()
        );
    }

    private String resolveLanguage() {
        Locale locale = LocaleContextHolder.getLocale();
        var languageTag = locale.toLanguageTag().toLowerCase(Locale.ROOT);
        return languageTag.startsWith("zh") ? "zh-CN" : "en-US";
    }
}
