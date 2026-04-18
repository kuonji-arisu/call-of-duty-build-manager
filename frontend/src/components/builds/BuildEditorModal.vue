<script setup lang="ts">
/* eslint-disable vue/no-mutating-props -- Parent owns a stable reactive form object; this modal edits its fields only. */
import { computed } from "vue";

import AttachmentRemoteSelect from "../attachments/AttachmentRemoteSelect.vue";
import WeaponRemoteSelect from "../weapons/WeaponRemoteSelect.vue";
import SelectionChips from "../common/SelectionChips.vue";
import { getGenerationOptions, getSlotLabel } from "../../shared/utils/labels";
import type { BuildEditorFormState } from "../../shared/buildEditor";
import type { AttachmentOption, Generation, Slot, WeaponOption } from "../../shared/types";

const props = withDefaults(
  defineProps<{
    show: boolean;
    form: BuildEditorFormState;
    title: string;
    source?: "admin" | "public";
    selectedWeapon: WeaponOption | null;
    selectedAttachments: Partial<Record<Slot, AttachmentOption>>;
    saving?: boolean;
    errorMessage?: string;
    disableWeaponChange?: boolean;
    weaponLockedHint?: string;
    saveText: string;
  }>(),
  {
    source: "admin",
    saving: false,
    errorMessage: "",
    disableWeaponChange: false,
    weaponLockedHint: "",
  },
);

const emit = defineEmits<{
  close: [];
  save: [];
  weaponUpdate: [weapon: WeaponOption | null];
  slotAttachmentIdUpdate: [slot: Slot, attachmentId: string];
  slotAttachmentUpdate: [slot: Slot, attachment: AttachmentOption | null];
}>();

const generationOptions = computed(() => {
  const options = getGenerationOptions();
  if (!props.selectedWeapon) {
    return options;
  }

  const visibleGenerations = new Set([...props.selectedWeapon.generations, ...props.form.generations]);
  return options.filter((option) => visibleGenerations.has(option.value));
});
const slotEntries = computed(() => props.selectedWeapon?.slots.map((slot) => ({ slot })) ?? []);

function updateGenerations(value: string[]) {
  props.form.generations = value as Generation[];
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

      <n-form label-placement="top">
        <div class="grid gap-4 md:grid-cols-2">
          <n-form-item class="md:col-span-2" label="所属武器">
            <div class="w-full">
              <WeaponRemoteSelect
                :value="props.form.weaponId"
                :selected-weapon="selectedWeapon"
                :disabled="disableWeaponChange"
                :source="source"
                placeholder="搜索并选择武器"
                @update:value="props.form.weaponId = $event"
                @update:selected-weapon="emit('weaponUpdate', $event)"
              />
              <p v-if="disableWeaponChange && weaponLockedHint" class="mt-2 text-xs text-slate-500">
                {{ weaponLockedHint }}
              </p>
            </div>
          </n-form-item>
          <n-form-item class="md:col-span-2" label="配装名称">
            <n-input :value="props.form.name" @update:value="props.form.name = $event" />
          </n-form-item>
          <n-form-item label="排序">
            <n-input-number
              :min="0"
              :value="props.form.sortOrder"
              class="w-full"
              @update:value="props.form.sortOrder = $event ?? 0"
            />
          </n-form-item>
          <n-form-item label="收藏">
            <n-checkbox
              :checked="props.form.isFavorite"
              @update:checked="props.form.isFavorite = $event"
            >
              标记为收藏配装
            </n-checkbox>
          </n-form-item>
          <n-form-item class="md:col-span-2" label="代际">
            <SelectionChips
              :model-value="props.form.generations"
              :options="generationOptions"
              @update:model-value="updateGenerations"
            />
          </n-form-item>
          <n-form-item class="md:col-span-2" label="备注">
            <n-input :value="props.form.notes" type="textarea" @update:value="props.form.notes = $event" />
          </n-form-item>
        </div>
      </n-form>

      <div class="space-y-4 rounded-2xl border border-white/8 bg-white/4 p-4">
        <div class="space-y-1">
          <p class="eyebrow-text">Slot Selectors</p>
          <h4 class="text-lg font-semibold text-slate-50">槽位配件</h4>
        </div>

        <div v-if="selectedWeapon" class="grid gap-4 lg:grid-cols-2">
          <n-form-item v-for="entry in slotEntries" :key="entry.slot" :label="getSlotLabel(entry.slot)">
            <AttachmentRemoteSelect
              :value="props.form.items[entry.slot] ?? ''"
              :selected-attachment="selectedAttachments[entry.slot] ?? null"
              :attachment-slot="entry.slot"
              :generations="props.form.generations"
              :weapon-id="props.form.weaponId"
              :source="source"
              placeholder="不选择配件"
              @update:value="emit('slotAttachmentIdUpdate', entry.slot, $event)"
              @update:selected-attachment="emit('slotAttachmentUpdate', entry.slot, $event)"
            />
          </n-form-item>
        </div>
        <p v-else class="text-sm text-slate-400">先选择武器，再为各槽位搜索可用配件。</p>
      </div>

      <div class="flex justify-end gap-3">
        <n-button round :disabled="saving" @click="emit('close')">取消</n-button>
        <n-button round type="primary" :loading="saving" @click="emit('save')">{{ saveText }}</n-button>
      </div>
    </div>
  </n-modal>
</template>
