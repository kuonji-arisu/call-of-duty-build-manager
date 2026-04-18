package io.github.kuonjiarisu.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kuonjiarisu.backend.mapper.AttachmentEffectDefinitionMapper;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinition;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinitionOption;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.command.AttachmentEffectDefinitionSaveCommand;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.PageSupport;

@Service
public class AttachmentEffectDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentEffectDefinitionService.class);

    private final AttachmentEffectDefinitionMapper attachmentEffectDefinitionMapper;
    private final ReferenceGuardService referenceGuardService;

    public AttachmentEffectDefinitionService(
        AttachmentEffectDefinitionMapper attachmentEffectDefinitionMapper,
        ReferenceGuardService referenceGuardService
    ) {
        this.attachmentEffectDefinitionMapper = attachmentEffectDefinitionMapper;
        this.referenceGuardService = referenceGuardService;
    }

    public List<AttachmentEffectDefinition> listAll() {
        return attachmentEffectDefinitionMapper.findAll();
    }

    public List<AttachmentEffectDefinitionOption> listOptions() {
        return attachmentEffectDefinitionMapper.findOptions();
    }

    public PageResult<AttachmentEffectDefinitionOption> searchOptions(Integer page, Integer pageSize, String keyword) {
        var result = listPage(page, pageSize, keyword);
        return new PageResult<>(
            result.items().stream()
                .map(definition -> new AttachmentEffectDefinitionOption(
                    definition.id(),
                    definition.label(),
                    definition.sortOrder()
                ))
                .toList(),
            result.total(),
            result.page(),
            result.pageSize()
        );
    }

    public PageResult<AttachmentEffectDefinition> listPage(Integer page, Integer pageSize, String keyword) {
        var normalizedPage = PageSupport.normalizePage(page);
        var normalizedPageSize = PageSupport.normalizePageSize(pageSize);
        var normalizedKeyword = PageSupport.normalizeText(keyword);

        return new PageResult<>(
            attachmentEffectDefinitionMapper.findPage(
                normalizedKeyword,
                normalizedPageSize,
                PageSupport.offset(normalizedPage, normalizedPageSize)
            ),
            attachmentEffectDefinitionMapper.countPage(normalizedKeyword),
            normalizedPage,
            normalizedPageSize
        );
    }

    @Transactional
    public AttachmentEffectDefinition create(AttachmentEffectDefinitionSaveCommand command) {
        return save(null, command);
    }

    @Transactional
    public AttachmentEffectDefinition update(String id, AttachmentEffectDefinitionSaveCommand command) {
        var definitionId = DomainSupport.requireText(id, "属性词条 ID");
        if (attachmentEffectDefinitionMapper.countById(definitionId) == 0) {
            throw new IllegalArgumentException("属性词条不存在");
        }
        return save(definitionId, command);
    }

    private AttachmentEffectDefinition save(String requestedId, AttachmentEffectDefinitionSaveCommand command) {
        var id = DomainSupport.keepOrGenerateId(requestedId, "attachment_effect_definition");
        var current = attachmentEffectDefinitionMapper.findById(id);
        var now = LocalDateTime.now();
        var normalized = new AttachmentEffectDefinition(
            id,
            DomainSupport.requireText(command.label(), "属性词条名称"),
            command.sortOrder() == null ? 0 : command.sortOrder(),
            current == null ? now : current.createdAt(),
            now
        );
        var existed = current != null;
        attachmentEffectDefinitionMapper.upsert(normalized);
        log.info(
            "Saved attachment effect definition: definitionId={} operation={}",
            normalized.id(),
            existed ? "update" : "create"
        );
        return normalized;
    }

    @Transactional
    public void delete(String id) {
        var definitionId = DomainSupport.requireText(id, "属性词条 ID");
        if (attachmentEffectDefinitionMapper.countById(definitionId) == 0) {
            throw new IllegalArgumentException("属性词条不存在");
        }
        referenceGuardService.ensureEffectDefinitionCanBeDeleted(definitionId);

        attachmentEffectDefinitionMapper.deleteById(definitionId);
        log.info("Deleted attachment effect definition: definitionId={}", definitionId);
    }
}
