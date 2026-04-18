package io.github.kuonjiarisu.backend.model;

import java.util.List;

public record AttachmentBindingUpdateRequest(
    String weaponId,
    List<String> attachmentIds,
    Boolean bound
) {
}
