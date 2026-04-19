import { createId } from "../utils/records";
import { getGenerationOptions } from "../utils/labels";
import type { Attachment, AttachmentEffectType, AttachmentTag, Generation, Slot } from "../types";

export interface AttachmentEffectRowState {
  rowId: string;
  definitionId: string;
  definitionLabel: string;
  effectType: AttachmentEffectType;
  level: 1 | 2 | 3 | 4;
}

export interface AttachmentFormState {
  id: string;
  name: string;
  subtitle: string;
  slot: Slot;
  generations: Generation[];
  tags: AttachmentTag[];
  effects: AttachmentEffectRowState[];
  sortOrder: number;
}

export function createEmptyEffectRow(): AttachmentEffectRowState {
  return {
    rowId: createId("attachment_effect_row"),
    definitionId: "",
    definitionLabel: "",
    effectType: "pro",
    level: 1,
  };
}

export function createEmptyAttachmentForm(): AttachmentFormState {
  return {
    id: "",
    name: "",
    subtitle: "",
    slot: "optic",
    generations: getGenerationOptions().slice(0, 1).map((option) => option.value),
    tags: [],
    effects: [],
    sortOrder: 0,
  };
}

export function cloneAttachmentToForm(attachment: Attachment): AttachmentFormState {
  return {
    id: attachment.id,
    name: attachment.name,
    subtitle: attachment.subtitle,
    slot: attachment.slot,
    generations: [...attachment.generations],
    tags: [...attachment.tags],
    effects: attachment.effects.map((effect) => ({
      rowId: createId("attachment_effect_row"),
      definitionId: effect.definitionId,
      definitionLabel: effect.label ?? effect.definitionId,
      effectType: effect.effectType,
      level: effect.level,
    })),
    sortOrder: attachment.sortOrder,
  };
}

export function createAttachmentFormSnapshot(form: AttachmentFormState) {
  return {
    id: form.id,
    name: form.name,
    subtitle: form.subtitle,
    slot: form.slot,
    generations: [...form.generations].sort(),
    tags: [...form.tags].sort(),
    effects: form.effects
      .map((effect) => ({
        definitionId: effect.definitionId,
        effectType: effect.effectType,
        level: effect.level,
      }))
      .sort((left, right) =>
        `${left.effectType}:${left.definitionId}:${left.level}`.localeCompare(
          `${right.effectType}:${right.definitionId}:${right.level}`,
        ),
      ),
    sortOrder: form.sortOrder,
  };
}
