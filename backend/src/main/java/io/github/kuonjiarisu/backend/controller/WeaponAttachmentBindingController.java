package io.github.kuonjiarisu.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.AttachmentBindingCandidate;
import io.github.kuonjiarisu.backend.model.AttachmentBindingUpdateRequest;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.service.attachment.AttachmentBindingService;

@RestController
@RequestMapping("/api/admin/weapon-attachment-bindings")
public class WeaponAttachmentBindingController {

    private final AttachmentBindingService attachmentBindingService;

    public WeaponAttachmentBindingController(AttachmentBindingService attachmentBindingService) {
        this.attachmentBindingService = attachmentBindingService;
    }

    @GetMapping
    public PageResult<AttachmentBindingCandidate> list(
        String weaponId,
        Integer page,
        Integer pageSize,
        String keyword,
        String slot,
        String generation,
        String tag,
        Boolean bound
    ) {
        return attachmentBindingService.listBindingCandidates(
            weaponId,
            page,
            pageSize,
            keyword,
            slot,
            generation,
            tag,
            bound
        );
    }

    @PostMapping
    public void update(@RequestBody AttachmentBindingUpdateRequest request) {
        attachmentBindingService.updateBindings(request);
    }
}
