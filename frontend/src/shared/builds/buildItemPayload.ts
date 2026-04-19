import { createId, nowIso } from "../utils/records";
import { requireText } from "../utils/validation";
import type {
  Build,
  BuildItem,
  BuildItemSavePayload,
  BuildSavePayload,
  RecommendedBuildSavePayload,
  Slot,
  WeaponOption,
} from "../types";
import type { BuildEditorFormState } from "../buildEditor";

export function buildBuildSavePayload(form: BuildEditorFormState): BuildSavePayload {
  return {
    weaponId: requireText(form.weaponId, "请选择所属武器"),
    name: requireText(form.name, "请输入配装名称"),
    generation: requireText(form.generation, "请选择配装代际"),
    notes: form.notes.trim() || null,
    sortOrder: Number.isNaN(form.sortOrder) ? 0 : form.sortOrder,
    isFavorite: form.isFavorite,
  };
}

export function buildLocalBuildRecord(
  form: BuildEditorFormState,
  currentBuild: Build | null,
): Build {
  const timestamp = nowIso();
  const payload = buildBuildSavePayload(form);
  return {
    ...payload,
    id: currentBuild?.id ?? createId("build"),
    createdAt: currentBuild?.createdAt ?? timestamp,
    updatedAt: timestamp,
  };
}

export function buildLocalBuildItemRecords(
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
): RecommendedBuildSavePayload {
  const build = buildBuildSavePayload(form);
  return {
    ...build,
    items: buildBuildItemSavePayload(form, weapon),
  };
}

function buildBuildItemSavePayload(
  form: BuildEditorFormState,
  weapon: WeaponOption,
): BuildItemSavePayload[] {
  return weapon.slots
    .map((slot) => {
      const attachmentId = form.items[slot];
      if (!attachmentId) {
        return null;
      }

      return {
        slot: slot as Slot,
        attachmentId,
      } satisfies BuildItemSavePayload;
    })
    .filter((item): item is BuildItemSavePayload => Boolean(item));
}
