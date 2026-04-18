package io.github.kuonjiarisu.backend.service.attachment;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kuonjiarisu.backend.mapper.AttachmentMapper;
import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentRow;
import io.github.kuonjiarisu.backend.model.OwnedStringValue;
import io.github.kuonjiarisu.backend.model.command.AttachmentSaveCommand;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.JsonStringListCodec;

@Service
public class AttachmentCommandService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentCommandService.class);

    private final AttachmentMapper attachmentMapper;
    private final AttachmentQueryService attachmentQueryService;
    private final JsonStringListCodec stringListCodec;
    private final AttachmentEffectNormalizer attachmentEffectNormalizer;
    private final AttachmentValidationService attachmentValidationService;
    private final ReferenceGuardService referenceGuardService;

    public AttachmentCommandService(
        AttachmentMapper attachmentMapper,
        AttachmentQueryService attachmentQueryService,
        JsonStringListCodec stringListCodec,
        AttachmentEffectNormalizer attachmentEffectNormalizer,
        AttachmentValidationService attachmentValidationService,
        ReferenceGuardService referenceGuardService
    ) {
        this.attachmentMapper = attachmentMapper;
        this.attachmentQueryService = attachmentQueryService;
        this.stringListCodec = stringListCodec;
        this.attachmentEffectNormalizer = attachmentEffectNormalizer;
        this.attachmentValidationService = attachmentValidationService;
        this.referenceGuardService = referenceGuardService;
    }

    @Transactional
    public Attachment create(AttachmentSaveCommand command) {
        return save(null, command);
    }

    @Transactional
    public Attachment update(String id, AttachmentSaveCommand command) {
        var attachmentId = DomainSupport.requireText(id, "配件 ID");
        if (attachmentMapper.countById(attachmentId) == 0) {
            throw new IllegalArgumentException("配件不存在");
        }
        return save(attachmentId, command);
    }

    private Attachment save(String requestedId, AttachmentSaveCommand command) {
        var id = DomainSupport.keepOrGenerateId(requestedId, "attachment");
        var currentRow = attachmentMapper.findRowById(id);
        var now = LocalDateTime.now();
        var normalized = new Attachment(
            id,
            DomainSupport.requireText(command.name(), "配件名称"),
            DomainSupport.requireText(command.subtitle(), "配件副标题"),
            DomainSupport.requireText(command.slot(), "配件槽位"),
            currentWeaponIds(id),
            DomainSupport.requireList(command.generations(), "配件代际"),
            DomainSupport.normalizeList(command.tags()),
            attachmentEffectNormalizer.normalize(command.effects()),
            command.sortOrder() == null ? 0 : command.sortOrder(),
            currentRow == null ? now : currentRow.createdAt(),
            now
        );
        var existed = currentRow != null;

        attachmentValidationService.validateExistingBindings(normalized.slot(), normalized.weaponIds());
        attachmentValidationService.validateEffectDefinitions(normalized.effects());
        attachmentValidationService.validateExistingBuildItemUsages(normalized);

        attachmentMapper.upsertRow(new AttachmentRow(
            normalized.id(),
            normalized.name(),
            normalized.subtitle(),
            normalized.slot(),
            stringListCodec.serialize(normalized.tags()),
            normalized.sortOrder(),
            normalized.createdAt(),
            normalized.updatedAt()
        ));
        attachmentMapper.deleteGenerationsByAttachmentId(normalized.id());
        attachmentMapper.deleteEffectsByAttachmentId(normalized.id());
        if (!normalized.generations().isEmpty()) {
            attachmentMapper.insertGenerations(normalized.id(), normalized.generations());
        }
        if (!normalized.effects().isEmpty()) {
            attachmentMapper.insertEffects(normalized.id(), normalized.effects());
        }
        log.info(
            "Saved attachment: attachmentId={} operation={} slot={} generationCount={} effectCount={}",
            normalized.id(),
            existed ? "update" : "create",
            normalized.slot(),
            normalized.generations().size(),
            normalized.effects().size()
        );
        return normalized;
    }

    @Transactional
    public void delete(String id) {
        var attachmentId = DomainSupport.requireText(id, "配件 ID");
        if (attachmentMapper.countById(attachmentId) == 0) {
            throw new IllegalArgumentException("配件不存在");
        }
        referenceGuardService.ensureAttachmentCanBeDeleted(attachmentId);

        attachmentMapper.deleteWeaponIdsByAttachmentId(attachmentId);
        attachmentMapper.deleteGenerationsByAttachmentId(attachmentId);
        attachmentMapper.deleteEffectsByAttachmentId(attachmentId);
        attachmentMapper.deleteById(attachmentId);
        log.info("Deleted attachment: attachmentId={}", attachmentId);
    }

    // 配件编辑不直接提交 weaponIds；绑定关系由独立绑定接口维护，避免保存基础信息时误清空绑定。
    private List<String> currentWeaponIds(String attachmentId) {
        if (attachmentId == null || attachmentId.isBlank() || attachmentMapper.countById(attachmentId) == 0) {
            return List.of();
        }
        return attachmentMapper.findWeaponIdsByAttachmentIds(List.of(attachmentId)).stream()
            .map(OwnedStringValue::value)
            .distinct()
            .toList();
    }
}
