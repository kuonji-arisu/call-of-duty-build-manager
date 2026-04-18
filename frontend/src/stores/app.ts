import { defineStore } from "pinia";

import { adminSettingsApi } from "../api/admin/settings";
import { loadInitialData } from "../api/public/bootstrap";
import { GENERATIONS, type Generation } from "../shared/types/common";
import type { AppInfo, AppSetting } from "../shared/types";
import { useBuildsStore } from "./builds";
import { useGenerationStore } from "./generation";

export const useAppStore = defineStore("app", {
  state: () => ({
    isLoading: false,
    isReady: false,
    errorMessage: "",
    settings: [] as AppSetting[],
    appInfo: {
      language: "zh-CN",
    } as AppInfo,
  }),
  getters: {
    libraryTitle(state) {
      return state.settings.find((setting) => setting.key === "libraryTitle")?.value ?? "COD Build Manager";
    },
  },
  actions: {
    getSettingValue(key: string) {
      return this.settings.find((setting) => setting.key === key)?.value;
    },
    async bootstrap() {
      if (this.isLoading) {
        return;
      }

      this.isLoading = true;
      this.errorMessage = "";

      try {
        const data = await loadInitialData();
        const generationStore = useGenerationStore();
        const buildsStore = useBuildsStore();

        this.appInfo = data.appInfo;
        buildsStore.initialize();
        this.settings = data.settings;

        const generationFilter = this.getSettingValue("homeGenerationFilter");
        if (generationFilter === "ALL" || GENERATIONS.includes(generationFilter as Generation)) {
          generationStore.setGenerationFilter(generationFilter as Generation | "ALL");
        }

        this.isReady = true;
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : String(error);
      } finally {
        this.isLoading = false;
      }
    },
    async reloadData() {
      this.isReady = false;
      await this.bootstrap();
    },
    async saveSetting(key: string, value: string) {
      const saved = await adminSettingsApi.saveSetting(key, value);
      const index = this.settings.findIndex((item) => item.key === saved.key);

      if (index >= 0) {
        this.settings.splice(index, 1, saved);
      } else {
        this.settings.push(saved);
      }

      if (key === "homeGenerationFilter") {
        const generationStore = useGenerationStore();
        if (value === "ALL" || GENERATIONS.includes(value as Generation)) {
          generationStore.setGenerationFilter(value as Generation | "ALL");
        }
      }

      return saved;
    },
  },
});
