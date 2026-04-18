package io.github.kuonjiarisu.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.AppSetting;
import io.github.kuonjiarisu.backend.model.SaveSettingRequest;
import io.github.kuonjiarisu.backend.service.SettingService;

@RestController
@RequestMapping("/api/admin/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public List<AppSetting> listSettings() {
        return settingService.listAll();
    }

    @PostMapping("/{key}")
    public AppSetting saveSetting(@PathVariable String key, @RequestBody SaveSettingRequest request) {
        return settingService.save(key, request.value());
    }
}
