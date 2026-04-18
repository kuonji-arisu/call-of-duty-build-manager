import { createId, nowIso } from "../utils/records";
import { requireSelection, requireText } from "../utils/validation";
import type { AttachmentEffect, AttachmentSavePayload, Generation } from "../types";
import type { AttachmentEffectRowState, AttachmentFormState } from "./attachmentForm";

export function normalizeAttachmentEffectRows(rows: AttachmentEffectRowState[]): AttachmentEffect[] {
  return rows
    .filter((row) => row.definitionId)
    .map((row) => ({
      definitionId: requireText(row.definitionId, "请选择属性词条"),
      effectType: row.effectType,
      level: row.level,
    }));
}

export function buildAttachmentSavePayload(form: AttachmentFormState): AttachmentSavePayload {
  const timestamp = nowIso();
  return {
    id: form.id || createId("attachment"),
    name: requireText(form.name, "请输入配件名称"),
    subtitle: requireText(form.subtitle, "请输入配件副标题"),
    slot: form.slot,
    generations: requireSelection(form.generations, "至少选择一个代际") as Generation[],
    tags: form.tags,
    effects: normalizeAttachmentEffectRows(form.effects),
    sortOrder: Number.isNaN(form.sortOrder) ? 0 : form.sortOrder,
    createdAt: form.createdAt || timestamp,
    updatedAt: timestamp,
  };
}
