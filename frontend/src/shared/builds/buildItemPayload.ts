import { createId, nowIso } from "../utils/records";
import { requireSelection, requireText } from "../utils/validation";
import type {
  BuildItem,
  BuildItemSavePayload,
  BuildSavePayload,
  Generation,
  RecommendedBuildSavePayload,
  Slot,
  WeaponOption,
} from "../types";
import type { BuildEditorFormState } from "../buildEditor";

export function buildBuildSavePayload(form: BuildEditorFormState): BuildSavePayload {
  const timestamp = nowIso();
  return {
    id: form.id || createId("build"),
    weaponId: requireText(form.weaponId, "请选择所属武器"),
    name: requireText(form.name, "请输入配装名称"),
    generations: requireSelection(form.generations, "至少选择一个代际") as Generation[],
    notes: form.notes.trim() || null,
    sortOrder: Number.isNaN(form.sortOrder) ? 0 : form.sortOrder,
    isFavorite: form.isFavorite,
    createdAt: form.createdAt || timestamp,
    updatedAt: timestamp,
  };
}

export function buildBuildItemPayload(
  buildId: string,
  form: BuildEditorFormState,
  weapon: WeaponOption,
  currentItems: BuildItem[],
): BuildItem[] {
  const timestamp = nowIso();
  return weapon.slots
    .map((slot) => {
      const attachmentId = form.items[slot];
      if (!attachmentId) {
        return null;
      }

      const existing = currentItems.find((item) => item.slot === slot);
      return {
        id: existing?.id ?? createId("build_item"),
        buildId,
        slot: slot as Slot,
        attachmentId,
        createdAt: existing?.createdAt ?? timestamp,
      } satisfies BuildItem;
    })
    .filter((item): item is BuildItem => Boolean(item));
}

export function buildRecommendedBuildSavePayload(
  form: BuildEditorFormState,
  weapon: WeaponOption,
  currentItems: BuildItem[],
): RecommendedBuildSavePayload {
  const build = buildBuildSavePayload(form);
  return {
    ...build,
    items: buildBuildItemSavePayload(form, weapon, currentItems),
  };
}

function buildBuildItemSavePayload(
  form: BuildEditorFormState,
  weapon: WeaponOption,
  currentItems: BuildItem[],
): BuildItemSavePayload[] {
  const timestamp = nowIso();
  return weapon.slots
    .map((slot) => {
      const attachmentId = form.items[slot];
      if (!attachmentId) {
        return null;
      }

      const existing = currentItems.find((item) => item.slot === slot);
      return {
        id: existing?.id ?? createId("build_item"),
        slot: slot as Slot,
        attachmentId,
        createdAt: existing?.createdAt ?? timestamp,
      } satisfies BuildItemSavePayload;
    })
    .filter((item): item is BuildItemSavePayload => Boolean(item));
}
