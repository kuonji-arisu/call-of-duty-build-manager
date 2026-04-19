package io.github.kuonjiarisu.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentOption;
import io.github.kuonjiarisu.backend.model.BuildDetail;
import io.github.kuonjiarisu.backend.model.BuildSummary;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.WeaponOption;
import io.github.kuonjiarisu.backend.service.attachment.AttachmentQueryService;
import io.github.kuonjiarisu.backend.service.build.BuildQueryService;
import io.github.kuonjiarisu.backend.service.weapon.WeaponQueryService;

@RestController
@RequestMapping("/api/public")
public class PublicLibraryController {

    private final WeaponQueryService weaponQueryService;
    private final AttachmentQueryService attachmentQueryService;
    private final BuildQueryService buildQueryService;

    public PublicLibraryController(
        WeaponQueryService weaponQueryService,
        AttachmentQueryService attachmentQueryService,
        BuildQueryService buildQueryService
    ) {
        this.weaponQueryService = weaponQueryService;
        this.attachmentQueryService = attachmentQueryService;
        this.buildQueryService = buildQueryService;
    }

    @GetMapping("/weapons")
    public PageResult<Weapon> listWeapons(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String weaponType,
        @RequestParam(required = false) String generation,
        @RequestParam(required = false) Boolean favorite
    ) {
        return weaponQueryService.listPage(page, pageSize, keyword, weaponType, generation, favorite);
    }

    @GetMapping("/weapons/options")
    public List<WeaponOption> listWeaponOptions() {
        return weaponQueryService.listOptions();
    }

    @GetMapping("/weapons/search-options")
    public PageResult<WeaponOption> searchWeaponOptions(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keyword
    ) {
        return weaponQueryService.searchOptions(page, pageSize, keyword);
    }

    @GetMapping("/weapons/{id}")
    public Weapon getWeapon(@PathVariable String id) {
        return weaponQueryService.findById(id);
    }

    @GetMapping("/weapons/{id}/attachments")
    public PageResult<Attachment> listWeaponAttachments(
        @PathVariable String id,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String slot,
        @RequestParam(required = false) String generation,
        @RequestParam(required = false) String tag
    ) {
        return attachmentQueryService.listAvailableForWeapon(id, page, pageSize, keyword, slot, generation, tag);
    }

    @GetMapping("/weapons/{id}/attachments/by-ids")
    public List<Attachment> listWeaponAttachmentsByIds(
        @PathVariable String id,
        @RequestParam List<String> ids
    ) {
        return attachmentQueryService.listAvailableByIdsForWeapon(id, ids);
    }

    @GetMapping("/weapons/{id}/attachment-options")
    public List<AttachmentOption> listWeaponAttachmentOptions(
        @PathVariable String id,
        @RequestParam(required = false) String slot
    ) {
        return attachmentQueryService.listAvailableOptionsForWeapon(id, slot);
    }

    @GetMapping("/weapons/{id}/attachment-search-options")
    public PageResult<AttachmentOption> searchWeaponAttachmentOptions(
        @PathVariable String id,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String slot,
        @RequestParam(required = false) String generation
    ) {
        return attachmentQueryService.searchAvailableOptionsForWeapon(id, page, pageSize, keyword, slot, generation);
    }

    @GetMapping("/attachments/options")
    public List<AttachmentOption> listAttachmentOptionsByIds(@RequestParam List<String> ids) {
        return attachmentQueryService.listOptionsByIds(ids);
    }

    @GetMapping("/weapons/{id}/builds")
    public PageResult<BuildSummary> listWeaponBuilds(
        @PathVariable String id,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String generation,
        @RequestParam(required = false) Boolean favorite
    ) {
        return buildQueryService.listPage(page, pageSize, keyword, id, generation, favorite);
    }

    @GetMapping("/builds/{id}")
    public BuildDetail getBuildDetail(@PathVariable String id) {
        return buildQueryService.findDetail(id);
    }
}
