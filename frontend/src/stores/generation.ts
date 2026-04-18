import { defineStore } from "pinia";

import type { GenerationFilter } from "../shared/types";

export const useGenerationStore = defineStore("generation", {
  state: () => ({
    generationFilter: "ALL" as GenerationFilter,
  }),
  actions: {
    setGenerationFilter(value: GenerationFilter) {
      this.generationFilter = value;
    },
  },
});
