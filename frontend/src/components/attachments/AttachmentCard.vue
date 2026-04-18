<script setup lang="ts">
import { computed } from "vue";

import AttachmentEffectList from "./AttachmentEffectList.vue";
import TagBadge from "../common/TagBadge.vue";
import type { Attachment } from "../../shared/types";
import {
  getAttachmentTagLabel,
  getGenerationLabel,
  getSlotLabel,
} from "../../shared/utils/labels";

const props = withDefaults(defineProps<{
  attachment: Attachment;
  effectsLimit?: number | null;
  showWeaponCount?: boolean;
  showSortOrder?: boolean;
}>(), {
  effectsLimit: 4,
  showWeaponCount: true,
  showSortOrder: true,
});

const hasPrestigeTag = computed(() => props.attachment.tags.includes("prestige"));
const subtitleText = computed(() => props.attachment.subtitle || getSlotLabel(props.attachment.slot));
</script>

<template>
  <article class="min-h-64 w-full rounded-2xl border border-white/8 bg-white/4 p-5 transition hover:bg-white/8">
    <div class="flex h-full flex-col gap-4">
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0 space-y-1">
          <h3 class="flex min-w-0 items-center gap-2 text-base font-semibold text-slate-50">
            <span class="truncate">{{ attachment.name }}</span>
            <svg
              v-if="hasPrestigeTag"
              aria-label="巅峰"
              class="h-4 w-5 shrink-0 translate-y-px text-cyan-300 drop-shadow-[0_0_8px_rgba(103,232,249,0.55)]"
              role="img"
              viewBox="0 0 64 48"
              xmlns="http://www.w3.org/2000/svg"
            >
              <title>巅峰</title>
              <path
                d="M12 4h40l10 12-30 28L2 16 12 4Z"
                fill="rgba(34, 211, 238, 0.16)"
                stroke="currentColor"
                stroke-linejoin="round"
                stroke-width="3"
              />
              <path
                d="M2 16h60M12 4l8 12 12-12 12 12 8-12M20 16l12 28 12-28"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="3"
              />
            </svg>
          </h3>
          <p class="truncate text-sm text-slate-400">{{ subtitleText }}</p>
        </div>
        <TagBadge v-if="showWeaponCount" tone="muted">{{ attachment.weaponIds.length }} 把武器</TagBadge>
      </div>

      <div class="flex flex-wrap gap-2">
        <TagBadge v-for="generation in attachment.generations" :key="generation" tone="muted">
          {{ getGenerationLabel(generation) }}
        </TagBadge>
        <TagBadge v-for="tag in attachment.tags" :key="tag" tone="accent">
          {{ getAttachmentTagLabel(tag) }}
        </TagBadge>
      </div>

      <div class="rounded-2xl border border-white/8 bg-black/10 p-3">
        <AttachmentEffectList
          :effects="effectsLimit == null ? attachment.effects : attachment.effects.slice(0, effectsLimit)"
          empty-text="暂无优缺点"
        />
        <p v-if="effectsLimit != null && attachment.effects.length > effectsLimit" class="mt-2 text-xs text-slate-500">
          还有 {{ attachment.effects.length - effectsLimit }} 条效果
        </p>
      </div>

      <div class="mt-auto flex items-center justify-between gap-3 pt-2">
        <span class="text-xs text-slate-500">{{ attachment.effects.length }} 条效果</span>
        <div class="flex gap-2">
          <slot name="actions" :attachment="attachment" />
        </div>
      </div>
    </div>
  </article>
</template>
