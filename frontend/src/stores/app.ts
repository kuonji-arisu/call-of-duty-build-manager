import { defineStore } from "pinia";

import { adminSettingsApi } from "../api/admin/settings";
import { loadInitialData } from "../api/public/bootstrap";
import { getGenerationOptions, setGenerationOptions } from "../shared/utils/labels";
import type { AppInfo, AppSetting, CatalogData, GenerationFilter } from "../shared/types";
import { useBuildsStore } from "./builds";
import { useGenerationStore } from "./generation";

export const useAppStore = defineStore("app", {
  state: () => ({
    isLoading: false,
    isReady: false,
    errorMessage: "",
    settings: [] as AppSetting[],
    catalog: {
      generations: [],
    } as CatalogData,
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
        this.catalog = data.catalog;
        setGenerationOptions(data.catalog.generations);
        buildsStore.initialize();
        this.settings = data.settings;

        const generationFilter = this.getSettingValue("homeGenerationFilter");
        if (generationFilter && isKnownGenerationFilter(generationFilter)) {
          generationStore.setGenerationFilter(generationFilter as GenerationFilter);
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
        if (isKnownGenerationFilter(value)) {
          generationStore.setGenerationFilter(value as GenerationFilter);
        }
      }

      return saved;
    },
  },
});

function isKnownGenerationFilter(value: string) {
  return value === "ALL" || getGenerationOptions().some((option) => option.value === value);
}
