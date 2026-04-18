<script setup lang="ts">
import { computed } from "vue";

import type { AttachmentEffect } from "../../shared/types";
import AttachmentEffectArrow from "./AttachmentEffectArrow.vue";

const props = withDefaults(defineProps<{
  effects: AttachmentEffect[];
  emptyText?: string;
}>(), {
  emptyText: "暂无属性变化。",
});

function sortEffects(left: NormalizedEffectEntry, right: NormalizedEffectEntry) {
  return left.sortOrder - right.sortOrder
    || left.label.localeCompare(right.label, "zh-CN");
}

const effectEntries = computed(() =>
  props.effects
    .map((effect) => {
      if (!effect.label) {
        return null;
      }

      return {
        ...effect,
        label: effect.label,
        sortOrder: effect.sortOrder ?? 0,
      };
    })
    .filter((entry): entry is NonNullable<typeof entry> => Boolean(entry))
);

type NormalizedEffectEntry = (typeof effectEntries.value)[number];

const proEntries = computed(() =>
  effectEntries.value
    .filter((entry) => entry.effectType === "pro")
    .sort(sortEffects),
);
const conEntries = computed(() =>
  effectEntries.value
    .filter((entry) => entry.effectType === "con")
    .sort(sortEffects),
);
const hasBothEffectTypes = computed(() => proEntries.value.length > 0 && conEntries.value.length > 0);
</script>

<template>
  <div
    v-if="effectEntries.length"
    class="grid grid-cols-1 gap-2 sm:gap-3"
    :class="hasBothEffectTypes ? 'sm:grid-cols-2' : ''"
  >
    <div v-if="proEntries.length" class="min-w-0 space-y-2">
      <div class="space-y-2">
        <div
          v-for="entry in proEntries"
          :key="`${entry.effectType}-${entry.definitionId}`"
          class="flex min-w-0 items-center gap-2 text-sm text-emerald-100"
        >
          <AttachmentEffectArrow :effect-type="entry.effectType" :level="entry.level" />
          <span class="min-w-0 leading-6">{{ entry.label }}</span>
        </div>
      </div>
    </div>

    <div v-if="conEntries.length" class="min-w-0 space-y-2">
      <div class="space-y-2">
        <div
          v-for="entry in conEntries"
          :key="`${entry.effectType}-${entry.definitionId}`"
          class="flex min-w-0 items-center gap-2 text-sm text-rose-100"
        >
          <AttachmentEffectArrow :effect-type="entry.effectType" :level="entry.level" />
          <span class="min-w-0 leading-6">{{ entry.label }}</span>
        </div>
      </div>
    </div>
  </div>
  <p v-else class="text-sm text-slate-400">{{ emptyText }}</p>
</template>
