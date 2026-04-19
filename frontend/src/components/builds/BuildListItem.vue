<script setup lang="ts">
import TagBadge from "../common/TagBadge.vue";
import type { Build, BuildSummary } from "../../shared/types";
import { getGenerationLabel } from "../../shared/utils/labels";

const props = defineProps<{
  build: Build | BuildSummary;
  weaponName?: string;
  itemCount?: number;
  selected?: boolean;
}>();

function getWeaponName() {
  return props.weaponName ?? ("weaponName" in props.build ? props.build.weaponName : "未知武器");
}

function getItemCount() {
  return props.itemCount ?? ("itemCount" in props.build ? props.build.itemCount : 0);
}
</script>

<template>
  <button
    class="w-full cursor-pointer rounded-2xl border border-white/8 bg-white/4 p-4 text-left transition hover:bg-white/8"
    :class="selected ? 'border-sky-400/50 bg-sky-500/10' : ''"
    type="button"
  >
    <div class="space-y-3">
      <div class="flex items-start justify-between gap-3">
        <div class="space-y-1">
          <h4 class="text-base font-semibold text-slate-50">{{ build.name }}</h4>
          <p class="text-sm text-slate-400">{{ getWeaponName() }}</p>
        </div>
        <TagBadge v-if="build.isFavorite" tone="accent">收藏</TagBadge>
      </div>

      <div class="flex flex-wrap gap-2">
        <TagBadge tone="muted">{{ getGenerationLabel(build.generation) }}</TagBadge>
        <TagBadge tone="default">{{ getItemCount() }} 个配件</TagBadge>
      </div>
    </div>
  </button>
</template>
