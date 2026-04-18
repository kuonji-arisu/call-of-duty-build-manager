package io.github.kuonjiarisu.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.mapper.SettingMapper;
import io.github.kuonjiarisu.backend.model.AppSetting;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class SettingService {

    private static final Logger log = LoggerFactory.getLogger(SettingService.class);

    private final SettingMapper settingMapper;

    public SettingService(SettingMapper settingMapper) {
        this.settingMapper = settingMapper;
    }

    public List<AppSetting> listAll() {
        return settingMapper.findAll();
    }

    public AppSetting save(String key, String value) {
        var normalized = new AppSetting(
            DomainSupport.requireText(key, "设置键"),
            DomainSupport.requireText(value, "设置值"),
            LocalDateTime.now()
        );
        settingMapper.upsert(normalized);
        log.info("Saved app setting: key={}", normalized.key());
        return settingMapper.findByKey(normalized.key());
    }
}
