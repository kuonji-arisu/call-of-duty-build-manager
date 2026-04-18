package io.github.kuonjiarisu.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentOption;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.request.AttachmentSaveRequest;
import io.github.kuonjiarisu.backend.service.attachment.AttachmentCommandService;
import io.github.kuonjiarisu.backend.service.attachment.AttachmentQueryService;

@RestController
@RequestMapping("/api/admin/attachments")
public class AttachmentController {

    private final AttachmentQueryService attachmentQueryService;
    private final AttachmentCommandService attachmentCommandService;

    public AttachmentController(
        AttachmentQueryService attachmentQueryService,
        AttachmentCommandService attachmentCommandService
    ) {
        this.attachmentQueryService = attachmentQueryService;
        this.attachmentCommandService = attachmentCommandService;
    }

    @GetMapping
    public PageResult<Attachment> list(
        Integer page,
        Integer pageSize,
        String keyword,
        String slot,
        String generation,
        String tag,
        String weaponId
    ) {
        return attachmentQueryService.listPage(page, pageSize, keyword, slot, generation, tag, weaponId);
    }

    @GetMapping("/weapon-options/{weaponId}")
    public PageResult<AttachmentOption> searchWeaponAttachmentOptions(
        @PathVariable String weaponId,
        Integer page,
        Integer pageSize,
        String keyword,
        String slot,
        @RequestParam(required = false) List<String> generations
    ) {
        return attachmentQueryService.searchAvailableOptionsForWeapon(weaponId, page, pageSize, keyword, slot, generations);
    }

    @PostMapping
    public Attachment create(@RequestBody AttachmentSaveRequest request) {
        return attachmentCommandService.create(request.toCommand());
    }

    @PutMapping("/{id}")
    public Attachment update(@PathVariable String id, @RequestBody AttachmentSaveRequest request) {
        return attachmentCommandService.update(id, request.toCommand());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        attachmentCommandService.delete(id);
    }
}
