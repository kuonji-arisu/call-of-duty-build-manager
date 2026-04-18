<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import type { SelectOption } from "naive-ui";

import { adminEffectDefinitionsApi } from "../../api/admin/effectDefinitions";
import type { AttachmentEffectDefinitionOption } from "../../shared/types";
import { useGuardedRemoteSearch } from "../../shared/utils/remoteSearch";

const props = withDefaults(
  defineProps<{
    value: string;
    selectedLabel?: string;
    placeholder?: string;
    disabled?: boolean;
  }>(),
  {
    selectedLabel: "",
    placeholder: "搜索属性词条",
    disabled: false,
  },
);

const emit = defineEmits<{
  "update:value": [value: string];
  "update:selectedLabel": [value: string];
}>();

type DefinitionSelectOption = SelectOption & { definition: AttachmentEffectDefinitionOption };

const remoteOptions = ref<DefinitionSelectOption[]>([]);

function toOption(definition: AttachmentEffectDefinitionOption): DefinitionSelectOption {
  return {
    label: definition.label,
    value: definition.id,
    definition,
  };
}

const options = computed(() => {
  const byId = new Map<string, SelectOption>();
  if (props.value && props.selectedLabel) {
    byId.set(props.value, { label: props.selectedLabel, value: props.value });
  }
  for (const option of remoteOptions.value) {
    byId.set(String(option.value), option);
  }
  return [...byId.values()];
});

const { loading, search, debouncedSearch } = useGuardedRemoteSearch(
  (keyword) => adminEffectDefinitionsApi.searchAttachmentEffectDefinitionOptions({ keyword, pageSize: 30 }),
  (result) => {
    remoteOptions.value = result.items.map(toOption);
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
  emit("update:selectedLabel", nextValue ? String(option?.label ?? "") : "");
}

watch(
  () => props.selectedLabel,
  () => {
    if (!props.value || !props.selectedLabel) {
      return;
    }
    if (!remoteOptions.value.some((option) => option.value === props.value)) {
      remoteOptions.value = [
        {
          label: props.selectedLabel,
          value: props.value,
          definition: { id: props.value, label: props.selectedLabel, sortOrder: 0 },
        },
        ...remoteOptions.value,
      ];
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
    :disabled="disabled"
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
