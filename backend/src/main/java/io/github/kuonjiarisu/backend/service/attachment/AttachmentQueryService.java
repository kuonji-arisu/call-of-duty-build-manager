package io.github.kuonjiarisu.backend.service.attachment;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.mapper.AttachmentMapper;
import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentOption;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.service.WeaponService;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.PageSupport;

@Service
public class AttachmentQueryService {

    private final AttachmentMapper attachmentMapper;
    private final WeaponService weaponService;
    private final AttachmentHydrator attachmentHydrator;
    private final AttachmentAssembler attachmentAssembler;

    public AttachmentQueryService(
        AttachmentMapper attachmentMapper,
        WeaponService weaponService,
        AttachmentHydrator attachmentHydrator,
        AttachmentAssembler attachmentAssembler
    ) {
        this.attachmentMapper = attachmentMapper;
        this.weaponService = weaponService;
        this.attachmentHydrator = attachmentHydrator;
        this.attachmentAssembler = attachmentAssembler;
    }

    public List<Attachment> listAll() {
        return attachmentHydrator.hydrateAll(attachmentMapper.findAllRows());
    }

    public PageResult<Attachment> listPage(
        Integer page,
        Integer pageSize,
        String keyword,
        String slot,
        String generation,
        String tag,
        String weaponId
    ) {
        var normalizedPage = PageSupport.normalizePage(page);
        var normalizedPageSize = PageSupport.normalizePageSize(pageSize);
        var normalizedKeyword = PageSupport.normalizeText(keyword);
        var normalizedSlot = PageSupport.normalizeText(slot);
        var normalizedGeneration = PageSupport.normalizeText(generation);
        var normalizedTag = PageSupport.normalizeText(tag);
        var normalizedWeaponId = PageSupport.normalizeText(weaponId);
        var total = attachmentMapper.countRows(
            normalizedKeyword,
            normalizedSlot,
            normalizedGeneration,
            normalizedTag,
            normalizedWeaponId
        );
        var rows = attachmentMapper.findRows(
            normalizedKeyword,
            normalizedSlot,
            normalizedGeneration,
            normalizedTag,
            normalizedWeaponId,
            normalizedPageSize,
            PageSupport.offset(normalizedPage, normalizedPageSize)
        );

        return new PageResult<>(attachmentHydrator.hydrate(rows), total, normalizedPage, normalizedPageSize);
    }

    public PageResult<Attachment> listAvailableForWeapon(
        String weaponId,
        Integer page,
        Integer pageSize,
        String keyword,
        String slot,
        String generation,
        String tag
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

        var candidateGenerations = normalizedGeneration == null ? List.<String>of() : List.of(normalizedGeneration);
        var total = attachmentMapper.countAvailableRowsForWeapon(
            weapon.id(),
            weapon.slots(),
            weapon.generations(),
            candidateGenerations,
            normalizedKeyword,
            normalizedSlot,
            normalizedGeneration,
            normalizedTag
        );
        var rows = attachmentMapper.findAvailableRowsForWeapon(
            weapon.id(),
            weapon.slots(),
            weapon.generations(),
            candidateGenerations,
            normalizedKeyword,
            normalizedSlot,
            normalizedGeneration,
            normalizedTag,
            normalizedPageSize,
            PageSupport.offset(normalizedPage, normalizedPageSize)
        );

        return new PageResult<>(attachmentHydrator.hydrate(rows), total, normalizedPage, normalizedPageSize);
    }

    public List<Attachment> listAvailableByIdsForWeapon(String weaponId, List<String> ids) {
        var weapon = weaponService.findById(weaponId);
        var normalizedIds = DomainSupport.normalizeList(ids);
        if (normalizedIds.isEmpty() || weapon.slots().isEmpty() || weapon.generations().isEmpty()) {
            return List.of();
        }

        var attachmentsById = findByIds(normalizedIds);
        return normalizedIds.stream()
            .map(attachmentsById::get)
            .filter(Objects::nonNull)
            .filter(attachment -> isAvailableForWeapon(attachment, weapon.id(), weapon.slots(), weapon.generations()))
            .toList();
    }

    public Attachment findById(String id) {
        var attachmentId = DomainSupport.requireText(id, "配件 ID");
        var row = attachmentMapper.findRowById(attachmentId);
        if (row == null) {
            throw new IllegalArgumentException("配件不存在");
        }
        return attachmentHydrator.hydrate(List.of(row)).getFirst();
    }

    public Map<String, Attachment> findByIds(List<String> ids) {
        var normalizedIds = ids == null ? List.<String>of() : ids.stream().distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }
        return attachmentHydrator.hydrate(attachmentMapper.findRowsByIds(normalizedIds)).stream()
            .collect(Collectors.toMap(Attachment::id, attachment -> attachment));
    }

    public List<AttachmentOption> listAvailableOptionsForWeapon(String weaponId, String slot) {
        var weapon = weaponService.findById(weaponId);
        var normalizedSlot = PageSupport.normalizeText(slot);
        if (weapon.slots().isEmpty()
            || weapon.generations().isEmpty()
            || (normalizedSlot != null && !weapon.slots().contains(normalizedSlot))) {
            return List.of();
        }

        return attachmentMapper.findAvailableOptionRowsForWeapon(
                weapon.id(),
                weapon.slots(),
                weapon.generations(),
                normalizedSlot
            ).stream()
            .map(attachmentAssembler::toOption)
            .toList();
    }

    public PageResult<AttachmentOption> searchAvailableOptionsForWeapon(
        String weaponId,
        Integer page,
        Integer pageSize,
        String keyword,
        String slot,
        List<String> generations
    ) {
        var weapon = weaponService.findById(weaponId);
        var normalizedPage = PageSupport.normalizePage(page);
        var normalizedPageSize = PageSupport.normalizePageSize(pageSize);
        var normalizedKeyword = PageSupport.normalizeText(keyword);
        var normalizedSlot = PageSupport.normalizeText(slot);
        var candidateGenerations = DomainSupport.normalizeList(generations);
        if (weapon.slots().isEmpty()
            || weapon.generations().isEmpty()
            || (normalizedSlot != null && !weapon.slots().contains(normalizedSlot))
            || !weapon.generations().containsAll(candidateGenerations)) {
            return new PageResult<>(List.of(), 0, normalizedPage, normalizedPageSize);
        }

        var rows = attachmentMapper.findAvailableRowsForWeapon(
            weapon.id(),
            weapon.slots(),
            weapon.generations(),
            candidateGenerations,
            normalizedKeyword,
            normalizedSlot,
            null,
            null,
            normalizedPageSize,
            PageSupport.offset(normalizedPage, normalizedPageSize)
        );
        var total = attachmentMapper.countAvailableRowsForWeapon(
            weapon.id(),
            weapon.slots(),
            weapon.generations(),
            candidateGenerations,
            normalizedKeyword,
            normalizedSlot,
            null,
            null
        );
        return new PageResult<>(
            rows.stream().map(attachmentAssembler::toOption).toList(),
            total,
            normalizedPage,
            normalizedPageSize
        );
    }

    public List<AttachmentOption> listOptionsByIds(List<String> ids) {
        var normalizedIds = DomainSupport.normalizeList(ids);
        if (normalizedIds.isEmpty()) {
            return List.of();
        }
        return attachmentHydrator.hydrate(attachmentMapper.findRowsByIds(normalizedIds)).stream()
            .map(attachmentAssembler::toOption)
            .toList();
    }

    private boolean isAvailableForWeapon(
        Attachment attachment,
        String weaponId,
        List<String> weaponSlots,
        List<String> weaponGenerations
    ) {
        return weaponSlots.contains(attachment.slot())
            && attachment.weaponIds().contains(weaponId)
            && intersects(attachment.generations(), weaponGenerations);
    }

    private boolean intersects(List<String> left, List<String> right) {
        return left.stream().anyMatch(right::contains);
    }
}
