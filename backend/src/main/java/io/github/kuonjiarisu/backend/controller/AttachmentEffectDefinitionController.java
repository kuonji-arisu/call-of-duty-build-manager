package io.github.kuonjiarisu.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinition;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinitionOption;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.request.AttachmentEffectDefinitionSaveRequest;
import io.github.kuonjiarisu.backend.service.AttachmentEffectDefinitionService;

@RestController
@RequestMapping("/api/admin/attachment-effect-definitions")
public class AttachmentEffectDefinitionController {

    private final AttachmentEffectDefinitionService attachmentEffectDefinitionService;

    public AttachmentEffectDefinitionController(AttachmentEffectDefinitionService attachmentEffectDefinitionService) {
        this.attachmentEffectDefinitionService = attachmentEffectDefinitionService;
    }

    @GetMapping
    public PageResult<AttachmentEffectDefinition> list(
        Integer page,
        Integer pageSize,
        String keyword
    ) {
        return attachmentEffectDefinitionService.listPage(page, pageSize, keyword);
    }

    @GetMapping("/options")
    public List<AttachmentEffectDefinitionOption> listOptions() {
        return attachmentEffectDefinitionService.listOptions();
    }

    @GetMapping("/search-options")
    public PageResult<AttachmentEffectDefinitionOption> searchOptions(Integer page, Integer pageSize, String keyword) {
        return attachmentEffectDefinitionService.searchOptions(page, pageSize, keyword);
    }

    @PostMapping
    public AttachmentEffectDefinition create(@RequestBody AttachmentEffectDefinitionSaveRequest request) {
        return attachmentEffectDefinitionService.create(request.toCommand());
    }

    @PutMapping("/{id}")
    public AttachmentEffectDefinition update(@PathVariable String id, @RequestBody AttachmentEffectDefinitionSaveRequest request) {
        return attachmentEffectDefinitionService.update(id, request.toCommand());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        attachmentEffectDefinitionService.delete(id);
    }
}
