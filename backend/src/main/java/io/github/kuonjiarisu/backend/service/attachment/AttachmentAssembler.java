package io.github.kuonjiarisu.backend.service.attachment;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.kuonjiarisu.backend.model.Attachment;
import io.github.kuonjiarisu.backend.model.AttachmentEffect;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinition;
import io.github.kuonjiarisu.backend.model.AttachmentEffectRow;
import io.github.kuonjiarisu.backend.model.AttachmentOption;
import io.github.kuonjiarisu.backend.model.AttachmentRow;
import io.github.kuonjiarisu.backend.support.JsonStringListCodec;

@Component
public class AttachmentAssembler {

    private final JsonStringListCodec stringListCodec;

    public AttachmentAssembler(JsonStringListCodec stringListCodec) {
        this.stringListCodec = stringListCodec;
    }

    public Attachment toAttachment(
        AttachmentRow row,
        Map<String, List<String>> weaponIdsByAttachmentId,
        Map<String, List<String>> generationsByAttachmentId,
        Map<String, List<AttachmentEffect>> effectsByAttachmentId
    ) {
        return new Attachment(
            row.id(),
            row.name(),
            row.subtitle(),
            row.slot(),
            weaponIdsByAttachmentId.getOrDefault(row.id(), List.of()),
            generationsByAttachmentId.getOrDefault(row.id(), List.of()),
            stringListCodec.parse(row.tagsJson()),
            effectsByAttachmentId.getOrDefault(row.id(), List.of()),
            row.sortOrder(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    public AttachmentOption toOption(Attachment attachment) {
        return new AttachmentOption(attachment.id(), attachment.name(), attachment.slot());
    }

    public AttachmentOption toOption(AttachmentRow row) {
        return new AttachmentOption(row.id(), row.name(), row.slot());
    }

    public AttachmentEffect toEffect(
        AttachmentEffectRow row,
        Map<String, AttachmentEffectDefinition> definitionsById
    ) {
        var definition = definitionsById.get(row.definitionId());
        return new AttachmentEffect(
            row.definitionId(),
            row.effectType(),
            row.level(),
            definition == null ? null : definition.label(),
            definition == null ? null : definition.sortOrder()
        );
    }
}
