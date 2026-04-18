import type {
  Attachment,
  GenerationFilter,
  Slot,
  Weapon,
} from "../types";
import {
  intersectsGenerations,
  matchesGenerationFilter,
} from "./generationFilter";

export function getAvailableAttachmentsForWeapon(
  weapon: Weapon,
  attachments: Attachment[],
  generationFilter: GenerationFilter,
  slot?: Slot,
): Attachment[] {
  if (!matchesGenerationFilter(weapon.generations, generationFilter)) {
    return [];
  }

  return attachments
    .filter((attachment) => {
      if (slot && attachment.slot !== slot) {
        return false;
      }

      return (
        (attachment.weaponIds ?? []).includes(weapon.id) &&
        weapon.slots.includes(attachment.slot) &&
        intersectsGenerations(attachment.generations, weapon.generations) &&
        matchesGenerationFilter(attachment.generations, generationFilter)
      );
    })
    .sort(
      (left, right) =>
        left.sortOrder - right.sortOrder || left.name.localeCompare(right.name, "zh-CN"),
    );
}
