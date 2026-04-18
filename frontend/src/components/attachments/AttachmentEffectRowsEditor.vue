<script setup lang="ts">
import type { SelectOption } from "naive-ui";

import AdminEffectDefinitionSelect from "./AdminEffectDefinitionSelect.vue";
import EmptyState from "../common/EmptyState.vue";
import { toSelectOptions } from "../../shared/utils/naive";
import {
  getAttachmentEffectTypeLabel,
  getAttachmentEffectTypeOptions,
} from "../../shared/utils/labels";
import { createEmptyEffectRow, type AttachmentEffectRowState } from "../../shared/attachments/attachmentForm";

const effects = defineModel<AttachmentEffectRowState[]>({ required: true });
const attachmentEffectTypeOptions = toSelectOptions(getAttachmentEffectTypeOptions());
const effectLevelOptions: SelectOption[] = [
  { label: "LV1", value: 1 },
  { label: "LV2", value: 2 },
  { label: "LV3", value: 3 },
  { label: "LV4", value: 4 },
];

function addEffectRow() {
  effects.value.push(createEmptyEffectRow());
}

function removeEffectRow(rowId: string) {
  effects.value = effects.value.filter((item) => item.rowId !== rowId);
}
</script>

<template>
  <div class="space-y-4 rounded-2xl border border-white/8 bg-white/4 p-4">
    <div class="flex items-start justify-between gap-4">
      <div class="space-y-1">
        <p class="eyebrow-text">Effects</p>
        <h4 class="text-lg font-semibold text-slate-50">优缺点表</h4>
      </div>
      <n-button quaternary round @click="addEffectRow">新增一行</n-button>
    </div>

    <div v-if="effects.length" class="space-y-3">
      <div
        v-for="row in effects"
        :key="row.rowId"
        class="grid gap-3 rounded-2xl border border-white/8 bg-black/10 p-4 lg:grid-cols-[minmax(0,1.6fr)_140px_120px_auto]"
      >
        <AdminEffectDefinitionSelect
          v-model:value="row.definitionId"
          v-model:selected-label="row.definitionLabel"
          placeholder="选择属性词条"
        />
        <n-select v-model:value="row.effectType" :options="attachmentEffectTypeOptions" placeholder="方向" />
        <n-select v-model:value="row.level" :options="effectLevelOptions" placeholder="等级" />
        <n-button quaternary round @click="removeEffectRow(row.rowId)">删除</n-button>
        <p class="text-xs text-slate-500 lg:col-span-4">
          {{ row.definitionLabel || "未选择词条" }} / {{ getAttachmentEffectTypeLabel(row.effectType) }} / LV{{ row.level }}
        </p>
      </div>
    </div>

    <EmptyState v-else description="新增一行后，通过搜索选择属性词条并设置方向与等级。" title="还没有配置属性变化" />
  </div>
</template>
