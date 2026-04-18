package io.github.kuonjiarisu.backend.service.attachment;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kuonjiarisu.backend.mapper.AttachmentMapper;
import io.github.kuonjiarisu.backend.model.AttachmentBindingCandidate;
import io.github.kuonjiarisu.backend.model.AttachmentBindingUpdateRequest;
import io.github.kuonjiarisu.backend.model.AttachmentRow;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.service.WeaponService;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.JsonStringListCodec;
import io.github.kuonjiarisu.backend.support.PageSupport;

@Service
public class AttachmentBindingService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentBindingService.class);

    private final AttachmentMapper attachmentMapper;
    private final WeaponService weaponService;
    private final JsonStringListCodec stringListCodec;
    private final AttachmentHydrator attachmentHydrator;
    private final AttachmentQueryService attachmentQueryService;
    private final AttachmentValidationService attachmentValidationService;
    private final ReferenceGuardService referenceGuardService;

    public AttachmentBindingService(
        AttachmentMapper attachmentMapper,
        WeaponService weaponService,
        JsonStringListCodec stringListCodec,
        AttachmentHydrator attachmentHydrator,
        AttachmentQueryService attachmentQueryService,
        AttachmentValidationService attachmentValidationService,
        ReferenceGuardService referenceGuardService
    ) {
        this.attachmentMapper = attachmentMapper;
        this.weaponService = weaponService;
        this.stringListCodec = stringListCodec;
        this.attachmentHydrator = attachmentHydrator;
        this.attachmentQueryService = attachmentQueryService;
        this.attachmentValidationService = attachmentValidationService;
        this.referenceGuardService = referenceGuardService;
    }

    public PageResult<AttachmentBindingCandidate> listBindingCandidates(
        String weaponId,
        Integer page,
        Integer pageSize,
        String keyword,
        String slot,
        String generation,
        String tag,
        Boolean bound
    ) {
        var weapon = weaponService.findById(weaponId);
        var normalizedPage = PageSupport.normalizePage(page);
        var normalizedPageSize = PageSupport.normalizePageSize(pageSize);
        var normalizedKeyword = PageSupport.normalizeText(keyword);
        var normalizedSlot = PageSupport.normalizeText(slot);
        var normalizedGeneration = PageSupport.normalizeText(generation);
        var normalizedTag = PageSupport.normalizeText(tag);
        if (weapon.slots().isEmpty()
            || weapon.generations().isEmpty()
            || (normalizedSlot != null && !weapon.slots().contains(normalizedSlot))
            || (normalizedGeneration != null && !weapon.generations().contains(normalizedGeneration))) {
            return new PageResult<>(List.of(), 0, normalizedPage, normalizedPageSize);
        }

        var rows = attachmentMapper.findBindingCandidateRows(
            weapon.id(),
            weapon.slots(),
            weapon.generations(),
            normalizedKeyword,
            normalizedSlot,
            normalizedGeneration,
            normalizedTag,
            bound,
            normalizedPageSize,
            PageSupport.offset(normalizedPage, normalizedPageSize)
        );
        var total = attachmentMapper.countBindingCandidateRows(
            weapon.id(),
            weapon.slots(),
            weapon.generations(),
            normalizedKeyword,
            normalizedSlot,
            normalizedGeneration,
            normalizedTag,
            bound
        );
        if (rows.isEmpty()) {
            return new PageResult<>(List.of(), total, normalizedPage, normalizedPageSize);
        }

        var attachmentIds = rows.stream().map(AttachmentRow::id).toList();
        var boundIds = Set.copyOf(attachmentMapper.findAttachmentIdsBoundToWeapon(weapon.id(), attachmentIds));
        var generationsByAttachmentId = attachmentHydrator.groupValues(
            attachmentMapper.findGenerationsByAttachmentIds(attachmentIds)
        );
        var candidates = rows.stream()
            .map(row -> new AttachmentBindingCandidate(
                row.id(),
                row.name(),
                row.slot(),
                generationsByAttachmentId.getOrDefault(row.id(), List.of()),
                stringListCodec.parse(row.tagsJson()),
                row.sortOrder(),
                boundIds.contains(row.id())
            ))
            .toList();
        return new PageResult<>(candidates, total, normalizedPage, normalizedPageSize);
    }

    @Transactional
    public void updateBindings(AttachmentBindingUpdateRequest request) {
        var weapon = weaponService.findById(request.weaponId());
        var attachmentIds = DomainSupport.requireList(request.attachmentIds(), "配件");
        var shouldBind = Boolean.TRUE.equals(request.bound());
        var attachmentsById = attachmentQueryService.findByIds(attachmentIds);
        if (attachmentsById.size() != attachmentIds.size()) {
            throw new IllegalArgumentException("部分配件不存在，无法更新绑定");
        }

        attachmentValidationService.validateBindingUpdate(weapon, attachmentsById, attachmentIds);

        // 绑定更新是录入配件可用性的关键边界，必须先整体校验，再批量写入或删除。
        if (shouldBind) {
            for (var attachmentId : attachmentIds) {
                attachmentMapper.insertWeaponBindingIfAbsent(attachmentId, weapon.id());
            }
            log.info(
                "Updated attachment bindings: weaponId={} operation=bind attachmentCount={}",
                weapon.id(),
                attachmentIds.size()
            );
            return;
        }

        referenceGuardService.ensureAttachmentBindingsCanBeRemoved(weapon.id(), attachmentIds);
        attachmentMapper.deleteWeaponBindingsByWeaponIdAndAttachmentIds(weapon.id(), attachmentIds);
        log.info(
            "Updated attachment bindings: weaponId={} operation=unbind attachmentCount={}",
            weapon.id(),
            attachmentIds.size()
        );
    }
}
