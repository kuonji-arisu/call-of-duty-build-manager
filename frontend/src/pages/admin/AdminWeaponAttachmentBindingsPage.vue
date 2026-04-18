<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useMessage } from "naive-ui";

import EmptyState from "../../components/common/EmptyState.vue";
import TagBadge from "../../components/common/TagBadge.vue";
import WeaponAttachmentAddModal from "../../components/weapons/WeaponAttachmentAddModal.vue";
import WeaponAttachmentSection from "../../components/weapons/WeaponAttachmentSection.vue";
import WeaponRemoteSelect from "../../components/weapons/WeaponRemoteSelect.vue";
import { adminAttachmentBindingsApi } from "../../api/admin/attachmentBindings";
import {
  getGenerationLabel,
  getSlotLabel,
  getWeaponTypeLabel,
} from "../../shared/utils/labels";
import type { Attachment, Slot, WeaponOption } from "../../shared/types";

const message = useMessage();
const selectedWeaponId = ref("");
const selectedWeapon = ref<WeaponOption | null>(null);
const lockedWeapon = ref<WeaponOption | null>(null);
const currentSlot = ref<Slot | null>(null);
const pageErrorMessage = ref("");
const addErrorMessage = ref("");
const addModalVisible = ref(false);
const binding = ref(false);
const unbindingId = ref("");
const attachmentSection = ref<InstanceType<typeof WeaponAttachmentSection> | null>(null);

const currentSlotLabel = computed(() => (currentSlot.value ? getSlotLabel(currentSlot.value) : ""));

function cloneWeaponOption(weapon: WeaponOption): WeaponOption {
  return {
    ...weapon,
    generations: [...weapon.generations],
    slots: [...weapon.slots],
  };
}

function applySelectedWeapon(weapon: WeaponOption | null) {
  if (!weapon) {
    lockedWeapon.value = null;
    currentSlot.value = null;
    pageErrorMessage.value = "";
    addErrorMessage.value = "";
    addModalVisible.value = false;
    return;
  }

  const nextWeapon = cloneWeaponOption(weapon);
  lockedWeapon.value = nextWeapon;
  currentSlot.value = currentSlot.value && nextWeapon.slots.includes(currentSlot.value)
    ? currentSlot.value
    : (nextWeapon.slots[0] ?? null);
  pageErrorMessage.value = "";
  addErrorMessage.value = "";
  addModalVisible.value = false;
}

function selectSlot(slot: Slot) {
  currentSlot.value = slot;
  pageErrorMessage.value = "";
  addErrorMessage.value = "";
}

function openAddModal() {
  addErrorMessage.value = "";
  addModalVisible.value = true;
}

function closeAddModal() {
  if (!binding.value) {
    addModalVisible.value = false;
    addErrorMessage.value = "";
  }
}

async function bindAttachments(attachmentIds: string[]) {
  if (!lockedWeapon.value || !attachmentIds.length) {
    return;
  }

  binding.value = true;
  addErrorMessage.value = "";
  try {
    await adminAttachmentBindingsApi.updateAttachmentBindings({
      weaponId: lockedWeapon.value.id,
      attachmentIds,
      bound: true,
    });
    await attachmentSection.value?.load();
    addModalVisible.value = false;
    message.success("配件已添加。");
  } catch (error) {
    addErrorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    binding.value = false;
  }
}

async function unbindAttachment(attachment: Attachment) {
  if (!lockedWeapon.value) {
    return;
  }

  unbindingId.value = attachment.id;
  pageErrorMessage.value = "";
  try {
    await adminAttachmentBindingsApi.updateAttachmentBindings({
      weaponId: lockedWeapon.value.id,
      attachmentIds: [attachment.id],
      bound: false,
    });
    await attachmentSection.value?.refreshAfterDelete();
    message.success("绑定已移除。");
  } catch (error) {
    pageErrorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    unbindingId.value = "";
  }
}

watch(selectedWeapon, applySelectedWeapon);
</script>

<template>
  <section class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="space-y-2">
        <p class="eyebrow-text">Admin / Weapon Attachment Bindings</p>
        <h1 class="page-title">武器配件绑定</h1>
        <p class="max-w-2xl text-sm leading-7 text-slate-300">
          按武器和槽位维护可用配件，配件本体编辑不承担绑定维护。
        </p>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-4">
        <n-form class="max-w-xl" label-placement="top">
          <n-form-item label="武器">
            <WeaponRemoteSelect
              v-model:value="selectedWeaponId"
              v-model:selected-weapon="selectedWeapon"
              placeholder="搜索并选择武器"
            />
          </n-form-item>
        </n-form>

        <div v-if="lockedWeapon" class="flex flex-wrap items-center gap-2 text-sm text-slate-300">
          <span>当前武器：</span>
          <span class="font-medium text-slate-50">{{ lockedWeapon.name }}</span>
          <span class="text-slate-500">/</span>
          <span>{{ getWeaponTypeLabel(lockedWeapon.weaponType) }}</span>
          <TagBadge v-for="generation in lockedWeapon.generations" :key="generation" tone="muted">
            {{ getGenerationLabel(generation) }}
          </TagBadge>
        </div>
      </div>
    </n-card>

    <n-card v-if="lockedWeapon" :bordered="false" class="glass-panel card-xl">
      <div class="space-y-5">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div class="space-y-1">
            <p class="eyebrow-text">Slots</p>
            <h3 class="text-xl font-semibold text-slate-50">槽位维护</h3>
            <p v-if="currentSlot" class="text-sm text-slate-400">当前槽位：{{ currentSlotLabel }}</p>
          </div>

          <div v-if="lockedWeapon.slots.length" class="flex flex-wrap gap-2">
            <n-button v-for="slot in lockedWeapon.slots" :key="slot" round :secondary="slot !== currentSlot"
              :type="slot === currentSlot ? 'primary' : 'default'" @click="selectSlot(slot)">
              {{ getSlotLabel(slot) }}
            </n-button>
          </div>
        </div>

        <n-alert v-if="pageErrorMessage" :bordered="false" type="error">
          {{ pageErrorMessage }}
        </n-alert>

        <WeaponAttachmentSection v-if="currentSlot" ref="attachmentSection" :weapon="lockedWeapon"
          :fixed-slot="currentSlot" title="已绑定配件" eyebrow="Bound Attachments" label="已绑定配件" empty-title="暂无绑定配件"
          empty-description="当前槽位还没有绑定配件。" :show-filters="false">
          <template #card-actions="{ attachment }">
            <n-button quaternary round size="small" type="error" :loading="unbindingId === attachment.id"
              @click="unbindAttachment(attachment)">
              解绑
            </n-button>
          </template>

          <template #append>
            <button
              class="flex min-h-64 w-full flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-cyan-300/40 bg-cyan-300/6 p-5 text-cyan-100 transition hover:border-cyan-200 hover:bg-cyan-300/10 disabled:cursor-not-allowed disabled:opacity-60"
              type="button" :disabled="binding" @click="openAddModal">
              <span class="text-4xl leading-none">+</span>
              <span class="text-sm font-medium">添加配件</span>
            </button>
          </template>
        </WeaponAttachmentSection>

        <EmptyState v-else description="这把武器没有可维护槽位。" title="暂无槽位" />
      </div>
    </n-card>

    <EmptyState v-else description="先搜索并选择一把武器，再维护它的配件绑定。" title="请选择武器" />

    <WeaponAttachmentAddModal
      :show="addModalVisible"
      :weapon="lockedWeapon"
      :attachment-slot="currentSlot"
      :saving="binding"
      :error-message="addErrorMessage"
      @close="closeAddModal"
      @submit="bindAttachments"
    />
  </section>
</template>
