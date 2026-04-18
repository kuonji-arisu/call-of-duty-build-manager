<script setup lang="ts">
import TagBadge from "../common/TagBadge.vue";
import type { Weapon } from "../../shared/types";
import {
  getGenerationLabel,
  getWeaponTagLabel,
  getWeaponTypeLabel,
} from "../../shared/utils/labels";

withDefaults(defineProps<{
  weapon: Weapon;
  showSortOrder?: boolean;
}>(), {
  showSortOrder: true,
});
</script>

<template>
  <article class="min-h-48 w-full rounded-2xl border border-white/8 bg-white/4 p-5 transition hover:bg-white/8">
    <div class="flex h-full flex-col gap-4">
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0 space-y-1">
          <h3 class="truncate text-base font-semibold text-slate-50">{{ weapon.name }}</h3>
          <p class="text-sm text-slate-400">{{ getWeaponTypeLabel(weapon.weaponType) }}</p>
        </div>
        <TagBadge v-if="weapon.isFavorite" tone="accent">收藏</TagBadge>
      </div>

      <div class="flex flex-wrap gap-2">
        <TagBadge v-for="generation in weapon.generations" :key="generation" tone="muted">
          {{ getGenerationLabel(generation) }}
        </TagBadge>
        <TagBadge v-for="tag in weapon.tags" :key="tag" tone="accent">
          {{ getWeaponTagLabel(tag) }}
        </TagBadge>
      </div>

      <div class="mt-auto flex items-center justify-between gap-3 pt-2">
        <span class="text-xs text-slate-500">{{ weapon.slots.length }} 个槽位</span>
        <div class="flex gap-2">
          <slot name="actions" :weapon="weapon">
            <RouterLink :to="`/weapons/${weapon.id}`">
              <n-button quaternary round size="small">查看详情</n-button>
            </RouterLink>
          </slot>
        </div>
      </div>
    </div>
  </article>
</template>
