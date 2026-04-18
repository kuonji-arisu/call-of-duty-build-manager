<script setup lang="ts">
import TagBadge from "../common/TagBadge.vue";
import type { Build } from "../../shared/types";
import { getGenerationLabel } from "../../shared/utils/labels";

withDefaults(defineProps<{
  build: Build;
  weaponName: string;
  itemCount: number;
  showNotes?: boolean;
  showSortOrder?: boolean;
}>(), {
  showNotes: true,
  showSortOrder: true,
});
</script>

<template>
  <article class="flex h-full min-h-48 w-full rounded-2xl border border-white/8 bg-white/4 p-5 transition hover:bg-white/8">
    <div class="flex h-full w-full flex-col gap-4">
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0 space-y-1">
          <h3 class="truncate text-base font-semibold text-slate-50">{{ build.name }}</h3>
          <p class="truncate text-sm text-slate-400">{{ weaponName }}</p>
        </div>
        <TagBadge v-if="build.isFavorite" tone="accent">收藏</TagBadge>
      </div>

      <div class="flex flex-wrap gap-2">
        <TagBadge v-for="generation in build.generations" :key="generation" tone="muted">
          {{ getGenerationLabel(generation) }}
        </TagBadge>
      </div>

      <p v-if="showNotes && build.notes" class="line-clamp-2 text-sm leading-6 text-slate-400">
        {{ build.notes }}
      </p>

      <div class="mt-auto flex items-center gap-3 pt-2">
        <span class="shrink-0 text-xs text-slate-500">{{ itemCount }} 个配件</span>
        <div class="ml-auto flex justify-end gap-2">
          <slot name="actions" :build="build" />
        </div>
      </div>
    </div>
  </article>
</template>
