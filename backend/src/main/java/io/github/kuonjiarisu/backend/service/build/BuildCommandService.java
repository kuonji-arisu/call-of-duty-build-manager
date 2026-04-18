package io.github.kuonjiarisu.backend.service.build;

import java.time.LocalDateTime;
import java.util.List;

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
import io.github.kuonjiarisu.backend.service.WeaponService;
import io.github.kuonjiarisu.backend.service.attachment.AttachmentQueryService;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class BuildCommandService {

    private static final Logger log = LoggerFactory.getLogger(BuildCommandService.class);

    private final BuildMapper buildMapper;
    private final WeaponService weaponService;
    private final AttachmentQueryService attachmentQueryService;
    private final BuildItemValidationService buildItemValidationService;

    public BuildCommandService(
        BuildMapper buildMapper,
        WeaponService weaponService,
        AttachmentQueryService attachmentQueryService,
        BuildItemValidationService buildItemValidationService
    ) {
        this.buildMapper = buildMapper;
        this.weaponService = weaponService;
        this.attachmentQueryService = attachmentQueryService;
        this.buildItemValidationService = buildItemValidationService;
    }

    @Transactional
    public Build save(BuildSaveCommand command) {
        var normalized = new Build(
            DomainSupport.keepOrGenerateId(command.id(), "build"),
            DomainSupport.requireText(command.weaponId(), "所属武器"),
            DomainSupport.requireText(command.name(), "配装名称"),
            DomainSupport.requireList(command.generations(), "配装代际"),
            command.notes() == null ? null : command.notes().trim(),
            command.sortOrder() == null ? 0 : command.sortOrder(),
            Boolean.TRUE.equals(command.isFavorite()),
            DomainSupport.keepOrNow(command.createdAt()),
            LocalDateTime.now()
        );
        var existed = buildMapper.countById(normalized.id()) > 0;
        var weapon = weaponService.findById(normalized.weaponId());
        var normalizedItems = normalizeItems(normalized.id(), command.items());
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
    private List<BuildItem> normalizeItems(String buildId, List<BuildItemSaveCommand> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
            .map(item -> new BuildItem(
                DomainSupport.keepOrGenerateId(item.id(), "build_item"),
                buildId,
                DomainSupport.requireText(item.slot(), "槽位"),
                DomainSupport.requireText(item.attachmentId(), "配件"),
                DomainSupport.keepOrNow(item.createdAt())
            ))
            .toList();
    }
}
