import type { Build, BuildItem } from "../shared/types";

const STORAGE_KEY = "cod-build-manager:user-builds:v1";

export interface PersistedUserBuilds {
  builds: Build[];
  buildItems: BuildItem[];
}

export const userBuildStorage = {
  load(): PersistedUserBuilds {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { builds: [], buildItems: [] };
    }

    try {
      const parsed = JSON.parse(raw) as Partial<PersistedUserBuilds>;
      return {
        builds: parsed.builds ?? [],
        buildItems: parsed.buildItems ?? [],
      };
    } catch {
      // 本地配装属于浏览器端数据，损坏时只回退为空列表，避免坏 JSON 阻断公共浏览流程。
      return { builds: [], buildItems: [] };
    }
  },
  save(value: PersistedUserBuilds) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
  },
};
