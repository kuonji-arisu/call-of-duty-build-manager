<script setup lang="ts">
import { computed } from "vue";

import AttachmentCard from "../attachments/AttachmentCard.vue";
import TagBadge from "../common/TagBadge.vue";
import { formatDateTime } from "../../shared/utils/formatting";
import {
  getGenerationLabel,
  getSlotLabel,
  getWeaponTypeLabel,
} from "../../shared/utils/labels";
import type { BuildDetail } from "../../shared/types";

const props = defineProps<{
  detail: BuildDetail;
  eyebrow: string;
  backLink: string;
  backText: string;
}>();

const build = computed(() => props.detail.build);
const weapon = computed(() => props.detail.weapon);
const itemsBySlot = computed(() =>
  new Map(props.detail.items.map((entry) => [entry.item.slot, entry])),
);
const buildSlots = computed(() =>
  weapon.value.slots.map((slot) => ({
    slot,
    entry: itemsBySlot.value.get(slot),
  })),
);
</script>

<template>
  <section class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="space-y-5">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div class="space-y-2">
            <p class="eyebrow-text">{{ eyebrow }}</p>
            <h1 class="page-title">{{ build.name }}</h1>
            <p class="text-base text-slate-300">
              {{ weapon.name }} / {{ getWeaponTypeLabel(weapon.weaponType) }}
            </p>
          </div>
          <RouterLink :to="backLink">
            <n-button quaternary round>{{ backText }}</n-button>
          </RouterLink>
        </div>

        <div class="flex flex-wrap gap-2">
          <TagBadge v-for="generation in build.generations" :key="generation" tone="accent">
            {{ getGenerationLabel(generation) }}
          </TagBadge>
          <TagBadge v-if="build.isFavorite" tone="muted">收藏</TagBadge>
        </div>
      </div>
    </n-card>

    <div class="grid gap-4 xl:grid-cols-2">
      <n-card :bordered="false" class="glass-panel card-lg">
        <div class="space-y-2">
          <p class="eyebrow-text">Notes</p>
          <p class="leading-7 text-slate-200">{{ build.notes || "暂无备注。" }}</p>
        </div>
      </n-card>

      <n-card :bordered="false" class="glass-panel card-lg">
        <div class="space-y-2">
          <p class="eyebrow-text">Meta</p>
          <p class="text-sm text-slate-300">创建时间：{{ formatDateTime(build.createdAt) }}</p>
          <p class="text-sm text-slate-300">更新时间：{{ formatDateTime(build.updatedAt) }}</p>
        </div>
      </n-card>
    </div>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-4">
        <div class="space-y-1">
          <p class="eyebrow-text">Slots</p>
          <h3 class="text-xl font-semibold text-slate-50">槽位配件明细</h3>
        </div>

        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <template v-for="entry in buildSlots" :key="entry.slot">
            <AttachmentCard
              v-if="entry.entry"
              :attachment="entry.entry.attachment"
              :effects-limit="null"
              :show-sort-order="false"
              :show-weapon-count="false"
            />
            <article
              v-else
              class="min-h-64 w-full rounded-2xl border border-white/8 bg-white/4 p-5"
            >
              <div class="flex h-full flex-col justify-between gap-4">
                <div class="space-y-2">
                  <h4 class="text-base font-semibold text-slate-50">未配置</h4>
                  <p class="text-sm text-slate-400">{{ getSlotLabel(entry.slot) }}</p>
                </div>
                <p class="text-xs text-slate-500">0 条效果</p>
              </div>
            </article>
          </template>
        </div>
      </div>
    </n-card>
  </section>
</template>
