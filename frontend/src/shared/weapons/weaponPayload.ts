import { requireSelection, requireText } from "../utils/validation";
import type { Generation, WeaponSavePayload, Slot, WeaponTag } from "../types";
import type { WeaponFormState } from "./weaponForm";

export function buildWeaponSavePayload(form: WeaponFormState): WeaponSavePayload {
  return {
    name: requireText(form.name, "请输入武器名称"),
    weaponType: form.weaponType,
    tags: form.tags as WeaponTag[],
    generations: requireSelection(form.generations, "至少选择一个代际") as Generation[],
    slots: requireSelection(form.slots, "至少选择一个槽位") as Slot[],
    sortOrder: Number.isNaN(form.sortOrder) ? 0 : form.sortOrder,
    isFavorite: form.isFavorite,
  };
}
