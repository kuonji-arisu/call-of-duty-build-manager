package io.github.kuonjiarisu.backend.service.attachment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.kuonjiarisu.backend.mapper.AttachmentMapper;
import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinition;
import io.github.kuonjiarisu.backend.model.AttachmentEffectRow;
import io.github.kuonjiarisu.backend.model.AttachmentRow;
import io.github.kuonjiarisu.backend.model.OwnedStringValue;
import io.github.kuonjiarisu.backend.service.AttachmentEffectDefinitionService;
import io.github.kuonjiarisu.backend.service.weapon.WeaponQueryService;
import io.github.kuonjiarisu.backend.support.OwnedStringValueSupport;

@Component
public class AttachmentHydrator {

    private static final Logger log = LoggerFactory.getLogger(AttachmentHydrator.class);

    private final AttachmentMapper attachmentMapper;
    private final WeaponQueryService weaponQueryService;
    private final AttachmentEffectDefinitionService attachmentEffectDefinitionService;
    private final AttachmentAssembler attachmentAssembler;
    public AttachmentHydrator(
        AttachmentMapper attachmentMapper,
        WeaponQueryService weaponQueryService,
        AttachmentEffectDefinitionService attachmentEffectDefinitionService,
        AttachmentAssembler attachmentAssembler
    ) {
        this.attachmentMapper = attachmentMapper;
        this.weaponQueryService = weaponQueryService;
        this.attachmentEffectDefinitionService = attachmentEffectDefinitionService;
        this.attachmentAssembler = attachmentAssembler;
    }

    public List<Attachment> hydrate(List<AttachmentRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        var ids = rows.stream().map(AttachmentRow::id).toList();
        var weaponIdRows = attachmentMapper.findWeaponIdsByAttachmentIds(ids);
        var existingWeaponIds = weaponQueryService.findByIds(
                weaponIdRows.stream()
                    .map(OwnedStringValue::value)
                    .distinct()
                    .toList()
            ).keySet();
        return hydrateWith(rows, existingWeaponIds);
    }

    public List<Attachment> hydrateAll(List<AttachmentRow> rows) {
        var existingWeaponIds = weaponQueryService.listAll().stream()
            .map(weapon -> weapon.id())
            .collect(Collectors.toSet());
        return hydrateWith(rows, existingWeaponIds);
    }

    private List<Attachment> hydrateWith(List<AttachmentRow> rows, Set<String> existingWeaponIds) {
        if (rows.isEmpty()) {
            return List.of();
        }

        var ids = rows.stream().map(AttachmentRow::id).toList();
        var definitionsById = attachmentEffectDefinitionService.listAll().stream()
            .collect(Collectors.toMap(AttachmentEffectDefinition::id, Function.identity()));
        var rawWeaponIdsByAttachmentId = OwnedStringValueSupport.groupValues(
            attachmentMapper.findWeaponIdsByAttachmentIds(ids)
        );
        warnMissingWeaponBindings(rawWeaponIdsByAttachmentId, existingWeaponIds);
        var weaponIdsByAttachmentId = filterValues(rawWeaponIdsByAttachmentId, existingWeaponIds);
        var generationsByAttachmentId = OwnedStringValueSupport.groupValues(
            attachmentMapper.findGenerationsByAttachmentIds(ids)
        );
        var effectRows = attachmentMapper.findEffectsByAttachmentIds(ids);
        warnMissingEffectDefinitions(effectRows, definitionsById);
        var effectsByAttachmentId = effectRows.stream()
            .filter(row -> definitionsById.containsKey(row.definitionId()))
            .collect(Collectors.groupingBy(
                AttachmentEffectRow::attachmentId,
                Collectors.mapping(row -> attachmentAssembler.toEffect(row, definitionsById), Collectors.toList())
            ));

        return rows.stream()
            .map(row -> attachmentAssembler.toAttachment(
                row,
                weaponIdsByAttachmentId,
                generationsByAttachmentId,
                effectsByAttachmentId
            ))
            .toList();
    }

    private void warnMissingWeaponBindings(
        Map<String, List<String>> weaponIdsByAttachmentId,
        Set<String> existingWeaponIds
    ) {
        var missingWeaponIds = weaponIdsByAttachmentId.values().stream()
            .flatMap(List::stream)
            .filter(weaponId -> !existingWeaponIds.contains(weaponId))
            .distinct()
            .toList();
        if (missingWeaponIds.isEmpty()) {
            return;
        }

        log.warn(
            "Skipped attachment bindings with missing weapons during hydration: missingWeaponCount={} sampleWeaponIds={}",
            missingWeaponIds.size(),
            missingWeaponIds.stream().limit(5).toList()
        );
    }

    private void warnMissingEffectDefinitions(
        List<AttachmentEffectRow> effectRows,
        Map<String, AttachmentEffectDefinition> definitionsById
    ) {
        var missingDefinitionIds = effectRows.stream()
            .map(AttachmentEffectRow::definitionId)
            .filter(definitionId -> !definitionsById.containsKey(definitionId))
            .distinct()
            .toList();
        if (missingDefinitionIds.isEmpty()) {
            return;
        }

        log.warn(
            "Skipped attachment effects with missing definitions during hydration: missingDefinitionCount={} sampleDefinitionIds={}",
            missingDefinitionIds.size(),
            missingDefinitionIds.stream().limit(5).toList()
        );
    }

    // 没有数据库外键兜底时，读取端只过滤不存在的武器引用；真实删除和修复仍交给维护操作处理。
    private Map<String, List<String>> filterValues(Map<String, List<String>> valuesByOwnerId, Set<String> allowedValues) {
        return valuesByOwnerId.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .filter(allowedValues::contains)
                    .toList()
            ));
    }
}
