<script setup lang="ts">
import { computed } from "vue";

import type { AttachmentEffectType } from "../../shared/types";

const props = defineProps<{
  effectType: AttachmentEffectType;
  level: 1 | 2 | 3 | 4;
}>();

const chevronText = computed(() => {
  return Array.from({ length: Math.min(props.level, 3) }, (_, index) => ({
    id: `${props.effectType}-${props.level}-${index}`,
  }));
});

const toneClass = computed(() =>
  props.effectType === "pro" ? "text-emerald-400" : "text-rose-400",
);
</script>

<template>
  <span :class="toneClass" class="inline-flex h-4 w-8 shrink-0 items-center justify-center">
    <span v-if="level < 4" class="inline-flex items-center">
      <svg v-for="item in chevronText" :key="item.id" :class="effectType === 'con' ? 'rotate-180' : ''"
        class="-ml-0.75 h-4 w-2.5 first:ml-0" fill="currentColor" viewBox="0 0 10 14">
        <path d="M0 0H3.4L10 7L3.4 14H0L5.8 7L0 0Z" />
      </svg>
    </span>

    <svg v-else :class="effectType === 'con' ? 'rotate-180' : ''" class="h-4 w-6" fill="currentColor"
      viewBox="0 1.5 25 12">
      <path d="M0 0.8H18.8L24.8 7.5L18.8 14.2H0L4.6 7.5L0 0.8ZM5.5 3.4H17.7L21.3 7.5L17.7 11.6H5.5L8.4 7.5L5.5 3.4Z"
        fill-rule="evenodd" />
    </svg>
  </span>
</template>
