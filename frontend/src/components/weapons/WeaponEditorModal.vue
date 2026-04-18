<script setup lang="ts">
/* eslint-disable vue/no-mutating-props -- Parent owns a stable reactive form object; this modal edits its fields only. */
import SelectionChips from "../common/SelectionChips.vue";
import {
  getGenerationOptions,
  getSlotOptions,
  getWeaponTagOptions,
  getWeaponTypeOptions,
} from "../../shared/utils/labels";
import { toSelectOptions } from "../../shared/utils/naive";
import type { Generation, Slot, WeaponTag } from "../../shared/types";
import type { WeaponFormState } from "../../shared/weapons/weaponForm";

const props = defineProps<{
  form: WeaponFormState;
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
const weaponTagOptions = getWeaponTagOptions();
const slotOptions = getSlotOptions();
const weaponTypeOptions = toSelectOptions(getWeaponTypeOptions());

function updateGenerations(value: string[]) {
  props.form.generations = value as Generation[];
}

function updateTags(value: string[]) {
  props.form.tags = value as WeaponTag[];
}

function updateSlots(value: string[]) {
  props.form.slots = value as Slot[];
}
</script>

<template>
  <n-modal
    :show="show"
    :mask-closable="false"
    preset="card"
    :title="title"
    class="max-w-3xl"
    @close="emit('close')"
  >
    <div class="space-y-5">
      <n-alert v-if="errorMessage" :bordered="false" type="error">
        {{ errorMessage }}
      </n-alert>

      <n-form label-placement="top">
        <div class="grid gap-4 md:grid-cols-2">
          <n-form-item class="md:col-span-2" label="武器名称">
            <n-input :value="props.form.name" @update:value="props.form.name = $event" />
          </n-form-item>
          <n-form-item label="武器分类">
            <n-select
              :value="props.form.weaponType"
              :options="weaponTypeOptions"
              @update:value="props.form.weaponType = $event"
            />
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
              :options="weaponTagOptions"
              @update:model-value="updateTags"
            />
          </n-form-item>
          <n-form-item class="md:col-span-2" label="槽位">
            <SelectionChips
              :model-value="props.form.slots"
              :options="slotOptions"
              @update:model-value="updateSlots"
            />
          </n-form-item>
          <n-form-item class="md:col-span-2" label="收藏">
            <n-checkbox
              :checked="props.form.isFavorite"
              @update:checked="props.form.isFavorite = $event"
            >
              标记为收藏武器
            </n-checkbox>
          </n-form-item>
        </div>
      </n-form>

      <div class="flex justify-end gap-3">
        <n-button round :disabled="saving" @click="emit('close')">取消</n-button>
        <n-button round type="primary" :loading="saving" @click="emit('save')">保存武器</n-button>
      </div>
    </div>
  </n-modal>
</template>
