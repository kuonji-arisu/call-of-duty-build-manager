package io.github.kuonjiarisu.backend.service.attachment;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.kuonjiarisu.backend.model.command.AttachmentEffectSaveCommand;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Component
public class AttachmentEffectNormalizer {

    public List<AttachmentEffectSaveCommand> normalize(List<AttachmentEffectSaveCommand> effects) {
        if (effects == null || effects.isEmpty()) {
            return List.of();
        }

        return effects.stream()
            .map(effect -> new AttachmentEffectSaveCommand(
                DomainSupport.requireText(effect.definitionId(), "属性词条"),
                requireEffectType(effect.effectType()),
                requireEffectLevel(effect.level())
            ))
            .distinct()
            .toList();
    }

    private String requireEffectType(String effectType) {
        var normalized = DomainSupport.requireText(effectType, "优缺点方向");
        if (!List.of("pro", "con").contains(normalized)) {
            throw new IllegalArgumentException("优缺点方向只能是 pro 或 con");
        }
        return normalized;
    }

    private Integer requireEffectLevel(Integer level) {
        if (level == null || level < 1 || level > 4) {
            throw new IllegalArgumentException("优缺点等级必须在 LV1 到 LV4 之间");
        }
        return level;
    }
}
