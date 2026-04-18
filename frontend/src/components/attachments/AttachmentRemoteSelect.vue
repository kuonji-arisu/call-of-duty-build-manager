<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import type { SelectOption } from "naive-ui";

import { adminAttachmentsApi } from "../../api/admin/attachments";
import { publicAttachmentsApi } from "../../api/public/attachments";
import type { AttachmentOption, Generation, Slot } from "../../shared/types";
import { getSlotLabel } from "../../shared/utils/labels";
import { useGuardedRemoteSearch } from "../../shared/utils/remoteSearch";

const props = withDefaults(
  defineProps<{
    value: string;
    source?: "admin" | "public";
    weaponId: string;
    attachmentSlot: Slot;
    generations?: Generation[];
    selectedAttachment?: AttachmentOption | null;
    placeholder?: string;
    disabled?: boolean;
  }>(),
  {
    source: "admin",
    generations: () => [],
    selectedAttachment: null,
    placeholder: "搜索配件",
    disabled: false,
  },
);

const emit = defineEmits<{
  "update:value": [value: string];
  "update:selectedAttachment": [value: AttachmentOption | null];
}>();

type AttachmentSelectOption = SelectOption & { attachment: AttachmentOption };

const remoteOptions = ref<AttachmentSelectOption[]>([]);

function toOption(attachment: AttachmentOption): AttachmentSelectOption {
  return {
    label: `${attachment.name} - ${getSlotLabel(attachment.slot)}`,
    value: attachment.id,
    attachment,
  };
}

const options = computed(() => {
  const byId = new Map<string, AttachmentSelectOption>();
  if (props.selectedAttachment) {
    byId.set(props.selectedAttachment.id, toOption(props.selectedAttachment));
  }
  for (const option of remoteOptions.value) {
    byId.set(String(option.value), option);
  }
  return [...byId.values()];
});

const { loading, search, debouncedSearch } = useGuardedRemoteSearch(
  async (keyword) => {
  if (!props.weaponId || !props.attachmentSlot) {
      return [];
  }

    const query = {
      keyword,
      slot: props.attachmentSlot,
      generations: props.generations,
      pageSize: 30,
    };
    const result = props.source === "public"
      ? await publicAttachmentsApi.searchWeaponAttachmentOptions(props.weaponId, query)
      : await adminAttachmentsApi.searchWeaponAttachmentOptions(props.weaponId, query);
    return result.items;
  },
  (attachments) => {
    remoteOptions.value = attachments.map(toOption);
  },
  {
    onError: () => {
      remoteOptions.value = [];
    },
  },
);

function handleUpdate(value: string | null, option: SelectOption | null) {
  const nextValue = value ?? "";
  emit("update:value", nextValue);
  emit("update:selectedAttachment", nextValue ? ((option as AttachmentSelectOption | null)?.attachment ?? null) : null);
}

watch(
  () => [props.weaponId, props.attachmentSlot, props.generations.join(",")],
  () => {
    remoteOptions.value = [];
    void search();
  },
);

watch(
  () => props.selectedAttachment,
  () => {
    if (props.selectedAttachment && !remoteOptions.value.some((option) => option.value === props.selectedAttachment?.id)) {
      remoteOptions.value = [toOption(props.selectedAttachment), ...remoteOptions.value];
    }
  },
);

onMounted(() => {
  void search();
});
</script>

<template>
  <n-select
    clearable
    :disabled="disabled || !weaponId"
    filterable
    :loading="loading"
    :options="options"
    :placeholder="placeholder"
    remote
    :value="value || null"
    @search="debouncedSearch"
    @update:value="handleUpdate"
  />
</template>
