<script setup lang="ts">
/* eslint-disable vue/no-mutating-props -- Parent owns a stable reactive form object; this modal edits its fields only. */
import AttachmentEffectRowsEditor from "./AttachmentEffectRowsEditor.vue";
import SelectionChips from "../common/SelectionChips.vue";
import { toSelectOptions } from "../../shared/utils/naive";
import {
  getAttachmentTagOptions,
  getGenerationOptions,
  getSlotOptions,
} from "../../shared/utils/labels";
import type { AttachmentTag, Generation } from "../../shared/types";
import type { AttachmentFormState } from "../../shared/attachments/attachmentForm";

const props = defineProps<{
  form: AttachmentFormState;
  show: boolean;
  title: string;
  saving: boolean;
  errorMessage: string;
}>();

const emit = defineEmits<{
  close: [];
  save: [];
}>();

const generationOptions = getGenerationOptions();
const attachmentTagOptions = getAttachmentTagOptions();
const slotOptions = toSelectOptions(getSlotOptions());

function updateGenerations(value: string[]) {
  props.form.generations = value as Generation[];
}

function updateTags(value: string[]) {
  props.form.tags = value as AttachmentTag[];
}
</script>

<template>
  <n-modal
    :show="show"
    :mask-closable="false"
    preset="card"
    :title="title"
    class="max-w-5xl"
    @close="emit('close')"
  >
    <div class="space-y-5">
      <n-alert v-if="errorMessage" :bordered="false" type="error">
        {{ errorMessage }}
      </n-alert>

      <n-alert :bordered="false" type="info">
        武器适用关系已独立到“武器配件绑定”维护。
      </n-alert>

      <n-form label-placement="top">
        <div class="grid gap-4 md:grid-cols-2">
          <n-form-item class="md:col-span-2" label="配件名称">
            <n-input :value="props.form.name" @update:value="props.form.name = $event" />
          </n-form-item>
          <n-form-item class="md:col-span-2" label="副标题">
            <n-input :value="props.form.subtitle" @update:value="props.form.subtitle = $event" />
          </n-form-item>
          <n-form-item label="槽位">
            <n-select :value="props.form.slot" :options="slotOptions" @update:value="props.form.slot = $event" />
          </n-form-item>
          <n-form-item label="排序">
            <n-input-number
              :min="0"
              :value="props.form.sortOrder"
              class="w-full"
              @update:value="props.form.sortOrder = $event ?? 0"
            />
          </n-form-item>
          <n-form-item class="md:col-span-2" label="代际">
            <SelectionChips
              :model-value="props.form.generations"
              :options="generationOptions"
              @update:model-value="updateGenerations"
            />
          </n-form-item>
          <n-form-item class="md:col-span-2" label="标签">
            <SelectionChips
              :model-value="props.form.tags"
              :options="attachmentTagOptions"
              @update:model-value="updateTags"
            />
          </n-form-item>
        </div>
      </n-form>

      <AttachmentEffectRowsEditor
        :model-value="props.form.effects"
        @update:model-value="props.form.effects = $event"
      />

      <div class="flex justify-end gap-3">
        <n-button round :disabled="saving" @click="emit('close')">取消</n-button>
        <n-button round type="primary" :loading="saving" @click="emit('save')">保存配件</n-button>
      </div>
    </div>
  </n-modal>
</template>
