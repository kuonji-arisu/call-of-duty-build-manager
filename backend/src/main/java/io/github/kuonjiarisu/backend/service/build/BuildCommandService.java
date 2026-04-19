package io.github.kuonjiarisu.backend.service.build;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kuonjiarisu.backend.mapper.BuildMapper;
import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.BuildRow;
import io.github.kuonjiarisu.backend.model.command.BuildItemSaveCommand;
import io.github.kuonjiarisu.backend.model.command.BuildSaveCommand;
import io.github.kuonjiarisu.backend.service.attachment.AttachmentQueryService;
import io.github.kuonjiarisu.backend.service.weapon.WeaponQueryService;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class BuildCommandService {

    private static final Logger log = LoggerFactory.getLogger(BuildCommandService.class);

    private final BuildMapper buildMapper;
    private final WeaponQueryService weaponQueryService;
    private final AttachmentQueryService attachmentQueryService;
    private final BuildItemValidationService buildItemValidationService;

    public BuildCommandService(
        BuildMapper buildMapper,
        WeaponQueryService weaponQueryService,
        AttachmentQueryService attachmentQueryService,
        BuildItemValidationService buildItemValidationService
    ) {
        this.buildMapper = buildMapper;
        this.weaponQueryService = weaponQueryService;
        this.attachmentQueryService = attachmentQueryService;
        this.buildItemValidationService = buildItemValidationService;
    }

    @Transactional
    public Build create(BuildSaveCommand command) {
        return save(null, command);
    }

    @Transactional
    public Build update(String id, BuildSaveCommand command) {
        var buildId = DomainSupport.requireText(id, "配装 ID");
        if (buildMapper.countById(buildId) == 0) {
            throw new IllegalArgumentException("配装不存在");
        }
        return save(buildId, command);
    }

    private Build save(String requestedId, BuildSaveCommand command) {
        var id = DomainSupport.keepOrGenerateId(requestedId, "build");
        var currentRow = buildMapper.findBuildRowById(id);
        var now = LocalDateTime.now();
        var normalized = new Build(
            id,
            DomainSupport.requireText(command.weaponId(), "所属武器"),
            DomainSupport.requireText(command.name(), "配装名称"),
            requireSingleGeneration(command.generations()),
            command.notes() == null ? null : command.notes().trim(),
            command.sortOrder() == null ? 0 : command.sortOrder(),
            Boolean.TRUE.equals(command.isFavorite()),
            currentRow == null ? now : currentRow.createdAt(),
            now
        );
        var existed = currentRow != null;
        var weapon = weaponQueryService.findById(normalized.weaponId());
        var existingItemsBySlot = buildMapper.findBuildItemsByBuildId(normalized.id()).stream()
            .collect(Collectors.toMap(BuildItem::slot, item -> item, (left, right) -> left));
        var normalizedItems = normalizeItems(normalized.id(), command.items(), existingItemsBySlot, now);
        buildItemValidationService.validateItems(
            normalized,
            weapon,
            normalizedItems,
            attachmentQueryService.findByIds(normalizedItems.stream().map(BuildItem::attachmentId).distinct().toList())
        );

        buildMapper.upsertBuildRow(new BuildRow(
            normalized.id(),
            normalized.weaponId(),
            normalized.name(),
            normalized.notes(),
            normalized.sortOrder(),
            normalized.isFavorite(),
            normalized.createdAt(),
            normalized.updatedAt()
        ));
        buildMapper.deleteGenerationsByBuildId(normalized.id());
        if (!normalized.generations().isEmpty()) {
            buildMapper.insertGenerations(normalized.id(), normalized.generations());
        }

        buildMapper.deleteItemsByBuildId(normalized.id());
        if (!normalizedItems.isEmpty()) {
            buildMapper.insertItems(normalizedItems);
        }
        log.info(
            "Saved recommended build: buildId={} weaponId={} operation={} generationCount={} itemCount={}",
            normalized.id(),
            normalized.weaponId(),
            existed ? "update" : "create",
            normalized.generations().size(),
            normalizedItems.size()
        );
        return normalized;
    }

    private List<String> requireSingleGeneration(List<String> generations) {
        var normalizedGenerations = DomainSupport.requireList(generations, "配装代际");
        if (normalizedGenerations.size() != 1) {
            throw new IllegalArgumentException("配装只能选择一个代际");
        }
        return normalizedGenerations;
    }

    @Transactional
    public void delete(String id) {
        var buildId = DomainSupport.requireText(id, "配装 ID");
        if (buildMapper.countById(buildId) == 0) {
            throw new IllegalArgumentException("配装不存在");
        }

        buildMapper.deleteItemsByBuildId(buildId);
        buildMapper.deleteGenerationsByBuildId(buildId);
        buildMapper.deleteBuildById(buildId);
        log.info("Deleted recommended build: buildId={}", buildId);
    }

    // 配装和配件项必须在同一个事务里整体替换，避免录入时出现“主表已保存、明细半旧半新”的状态。
    private List<BuildItem> normalizeItems(
        String buildId,
        List<BuildItemSaveCommand> items,
        Map<String, BuildItem> existingItemsBySlot,
        LocalDateTime now
    ) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
            .map(item -> {
                var slot = DomainSupport.requireText(item.slot(), "槽位");
                var existing = existingItemsBySlot.get(slot);
                return new BuildItem(
                    existing == null ? DomainSupport.keepOrGenerateId(null, "build_item") : existing.id(),
                    buildId,
                    slot,
                    DomainSupport.requireText(item.attachmentId(), "配件"),
                    existing == null ? now : existing.createdAt()
                );
            })
            .toList();
    }
}
