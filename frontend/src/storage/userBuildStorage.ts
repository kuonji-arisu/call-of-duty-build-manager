import {
  GENERATIONS,
  SLOTS,
  type Generation,
  type Slot,
} from "../shared/types/common";
import {
  type Build,
  type BuildItem,
} from "../shared/types";

const STORAGE_KEY = "cod-build-manager:user-builds:v1";
const generationSet = new Set<string>(GENERATIONS);
const slotSet = new Set<string>(SLOTS);

export interface PersistedUserBuilds {
  builds: Build[];
  buildItems: BuildItem[];
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function normalizedText(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function normalizedOptionalText(value: unknown): string | null {
  const text = normalizedText(value);
  return text || null;
}

function normalizedNumber(value: unknown): number {
  return typeof value === "number" && Number.isFinite(value) ? value : 0;
}

function normalizedBoolean(value: unknown): boolean {
  return value === true;
}

function normalizedIso(value: unknown, fallback: string): string {
  const text = normalizedText(value);
  return text && !Number.isNaN(Date.parse(text)) ? text : fallback;
}

function normalizedGenerations(value: unknown): Generation[] {
  if (!Array.isArray(value)) {
    return [];
  }

  return [...new Set(value.filter((item): item is Generation =>
    typeof item === "string" && generationSet.has(item),
  ))];
}

function normalizeBuild(value: unknown): Build | null {
  if (!isRecord(value)) {
    return null;
  }

  const id = normalizedText(value.id);
  const weaponId = normalizedText(value.weaponId);
  const name = normalizedText(value.name);
  const generations = normalizedGenerations(value.generations);
  if (!id || !weaponId || !name || !generations.length) {
    return null;
  }

  const createdAt = normalizedIso(value.createdAt, new Date(0).toISOString());
  return {
    id,
    weaponId,
    name,
    generations,
    notes: normalizedOptionalText(value.notes),
    sortOrder: normalizedNumber(value.sortOrder),
    isFavorite: normalizedBoolean(value.isFavorite),
    createdAt,
    updatedAt: normalizedIso(value.updatedAt, createdAt),
  };
}

function normalizeBuildItem(value: unknown, buildIds: Set<string>): BuildItem | null {
  if (!isRecord(value)) {
    return null;
  }

  const id = normalizedText(value.id);
  const buildId = normalizedText(value.buildId);
  const slot = normalizedText(value.slot);
  const attachmentId = normalizedText(value.attachmentId);
  if (!id || !buildIds.has(buildId) || !slotSet.has(slot) || !attachmentId) {
    return null;
  }

  return {
    id,
    buildId,
    slot: slot as Slot,
    attachmentId,
    createdAt: normalizedIso(value.createdAt, new Date(0).toISOString()),
  };
}

function normalizePersistedUserBuilds(value: unknown): PersistedUserBuilds {
  if (!isRecord(value)) {
    return { builds: [], buildItems: [] };
  }

  const seenBuildIds = new Set<string>();
  const builds = (Array.isArray(value.builds) ? value.builds : [])
    .map(normalizeBuild)
    .filter((build): build is Build => {
      if (!build || seenBuildIds.has(build.id)) {
        return false;
      }
      seenBuildIds.add(build.id);
      return true;
    });

  const buildIds = new Set(builds.map((build) => build.id));
  const seenItemIds = new Set<string>();
  const seenBuildSlots = new Set<string>();
  const buildItems = (Array.isArray(value.buildItems) ? value.buildItems : [])
    .map((item) => normalizeBuildItem(item, buildIds))
    .filter((item): item is BuildItem => {
      if (!item || seenItemIds.has(item.id)) {
        return false;
      }
      const buildSlotKey = `${item.buildId}:${item.slot}`;
      if (seenBuildSlots.has(buildSlotKey)) {
        return false;
      }
      seenItemIds.add(item.id);
      seenBuildSlots.add(buildSlotKey);
      return true;
    });

  return { builds, buildItems };
}

export const userBuildStorage = {
  load(): PersistedUserBuilds {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { builds: [], buildItems: [] };
    }

    try {
      return normalizePersistedUserBuilds(JSON.parse(raw) as unknown);
    } catch {
      // 本地配装属于浏览器端数据，损坏时只回退为空列表，避免坏 JSON 阻断公共浏览流程。
      return { builds: [], buildItems: [] };
    }
  },
  save(value: PersistedUserBuilds) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(normalizePersistedUserBuilds(value)));
  },
};
