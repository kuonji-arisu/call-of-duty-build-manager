import { defineStore } from "pinia";

import { matchesGenerationFilter } from "../shared/utils/generationFilter";
import type { Build, BuildItem, BuildSavePayload } from "../shared/types";
import { userBuildStorage } from "../storage/userBuildStorage";
import { useGenerationStore } from "./generation";

interface UserBuildState {
  builds: Build[];
  buildItems: BuildItem[];
  initialized: boolean;
}

export const useBuildsStore = defineStore("builds", {
  state: (): UserBuildState => ({
    builds: [],
    buildItems: [],
    initialized: false,
  }),
  getters: {
    sortedBuilds: (state) =>
      [...state.builds].sort(
        (left, right) =>
          left.sortOrder - right.sortOrder || left.name.localeCompare(right.name, "zh-CN"),
      ),
    visibleBuilds(): Build[] {
      const generationStore = useGenerationStore();

      return this.sortedBuilds.filter((build) =>
        matchesGenerationFilter(build.generations, generationStore.generationFilter),
      );
    },
  },
  actions: {
    initialize() {
      if (this.initialized) {
        return;
      }

      const persisted = userBuildStorage.load();
      this.builds = persisted.builds;
      this.buildItems = persisted.buildItems;
      this.initialized = true;
    },
    persist() {
      userBuildStorage.save({
        builds: this.builds,
        buildItems: this.buildItems,
      });
    },
    getBuildById(id: string) {
      this.initialize();
      return this.builds.find((build) => build.id === id);
    },
    getBuildsForWeapon(weaponId: string) {
      this.initialize();
      return this.visibleBuilds.filter((build) => build.weaponId === weaponId);
    },
    getBuildItems(buildId: string) {
      this.initialize();
      return this.buildItems.filter((item) => item.buildId === buildId);
    },
    async saveBuildWithItems(build: BuildSavePayload, items: BuildItem[]) {
      this.initialize();
      const buildIndex = this.builds.findIndex((item) => item.id === build.id);
      const nextBuilds = [...this.builds];
      const nextBuildItems = this.buildItems.filter((item) => item.buildId !== build.id);

      // 先构造完整快照并写入 localStorage，再替换内存状态，避免页面刷新时只保存了配装或只保存了明细。
      if (buildIndex >= 0) {
        nextBuilds.splice(buildIndex, 1, build);
      } else {
        nextBuilds.push(build);
      }
      nextBuildItems.push(...items);

      userBuildStorage.save({
        builds: nextBuilds,
        buildItems: nextBuildItems,
      });
      this.builds = nextBuilds;
      this.buildItems = nextBuildItems;

      return build;
    },
    async deleteBuild(buildId: string) {
      this.initialize();
      this.builds = this.builds.filter((build) => build.id !== buildId);
      this.buildItems = this.buildItems.filter((item) => item.buildId !== buildId);
      this.persist();
    },
  },
});
