package io.github.kuonjiarisu.backend.service.build;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.mapper.BuildMapper;
import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildDetail;
import io.github.kuonjiarisu.backend.model.BuildDetailItem;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.BuildSummary;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.service.attachment.AttachmentQueryService;
import io.github.kuonjiarisu.backend.service.reference.ReferenceGuardService;
import io.github.kuonjiarisu.backend.service.weapon.WeaponQueryService;
import io.github.kuonjiarisu.backend.support.DomainSupport;
import io.github.kuonjiarisu.backend.support.PageSupport;

@Service
public class BuildQueryService {

    private static final Logger log = LoggerFactory.getLogger(BuildQueryService.class);

    private final BuildMapper buildMapper;
    private final WeaponQueryService weaponQueryService;
    private final AttachmentQueryService attachmentQueryService;
    private final BuildHydrator buildHydrator;
    private final ReferenceGuardService referenceGuardService;

    public BuildQueryService(
        BuildMapper buildMapper,
        WeaponQueryService weaponQueryService,
        AttachmentQueryService attachmentQueryService,
        BuildHydrator buildHydrator,
        ReferenceGuardService referenceGuardService
    ) {
        this.buildMapper = buildMapper;
        this.weaponQueryService = weaponQueryService;
        this.attachmentQueryService = attachmentQueryService;
        this.buildHydrator = buildHydrator;
        this.referenceGuardService = referenceGuardService;
    }

    public PageResult<BuildSummary> listPage(
        Integer page,
        Integer pageSize,
        String keyword,
        String weaponId,
        String generation,
        Boolean favorite
    ) {
        var normalizedPage = PageSupport.normalizePage(page);
        var normalizedPageSize = PageSupport.normalizePageSize(pageSize);
        var normalizedKeyword = PageSupport.normalizeText(keyword);
        var normalizedWeaponId = PageSupport.normalizeText(weaponId);
        var normalizedGeneration = PageSupport.normalizeText(generation);
        var hideMissingWeapon = referenceGuardService.shouldHideMissingWeapon();
        var total = buildMapper.countBuildRows(
            normalizedKeyword,
            normalizedWeaponId,
            normalizedGeneration,
            favorite,
            hideMissingWeapon
        );
        var rows = buildMapper.findBuildRows(
            normalizedKeyword,
            normalizedWeaponId,
            normalizedGeneration,
            favorite,
            hideMissingWeapon,
            normalizedPageSize,
            PageSupport.offset(normalizedPage, normalizedPageSize)
        );

        return new PageResult<>(buildHydrator.toSummaries(rows), total, normalizedPage, normalizedPageSize);
    }

    public BuildDetail findDetail(String id) {
        var buildId = DomainSupport.requireText(id, "配装 ID");
        var row = buildMapper.findBuildRowById(buildId);
        if (row == null) {
            throw new IllegalArgumentException("配装不存在");
        }

        var weapon = referenceGuardService.requireReadableBuildWeapon(
            weaponQueryService.findByIds(List.of(row.weaponId())).get(row.weaponId())
        );
        var build = buildHydrator.toBuilds(List.of(row)).getFirst();
        if (!weapon.generations().containsAll(build.generations())) {
            log.warn(
                "Rejected build detail read because build generations are outside weapon generations: buildId={} weaponId={} buildGenerations={} weaponGenerations={}",
                build.id(),
                weapon.id(),
                build.generations(),
                weapon.generations()
            );
            throw new IllegalArgumentException("配装代际与所属武器不匹配");
        }
        var items = buildMapper.findBuildItemsByBuildId(build.id());
        var attachmentsById = attachmentQueryService.findByIds(
            items.stream()
                .map(BuildItem::attachmentId)
                .distinct()
                .toList()
        );
        var readableItems = items.stream()
            .filter(item -> referenceGuardService.isReadableBuildItem(
                item,
                build,
                weapon,
                attachmentsById.get(item.attachmentId())
            ))
            .toList();
        if (readableItems.size() != items.size()) {
            log.warn(
                "Skipped unreadable build items during detail read: buildId={} skippedCount={} totalCount={}",
                build.id(),
                items.size() - readableItems.size(),
                items.size()
            );
        }
        var detailItems = readableItems.stream()
            .map(item -> new BuildDetailItem(item, attachmentsById.get(item.attachmentId())))
            .toList();

        return new BuildDetail(build, weapon, detailItems);
    }
}
