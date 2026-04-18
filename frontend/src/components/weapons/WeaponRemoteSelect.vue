<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import type { SelectOption } from "naive-ui";

import { adminWeaponsApi } from "../../api/admin/weapons";
import { publicWeaponsApi } from "../../api/public/weapons";
import type { WeaponOption } from "../../shared/types";
import { getWeaponTypeLabel } from "../../shared/utils/labels";
import { useGuardedRemoteSearch } from "../../shared/utils/remoteSearch";

const props = withDefaults(
  defineProps<{
    value: string;
    source?: "admin" | "public";
    selectedWeapon?: WeaponOption | null;
    placeholder?: string;
    clearable?: boolean;
    disabled?: boolean;
  }>(),
  {
    source: "admin",
    selectedWeapon: null,
    placeholder: "搜索武器",
    clearable: true,
    disabled: false,
  },
);

const emit = defineEmits<{
  "update:value": [value: string];
  "update:selectedWeapon": [value: WeaponOption | null];
}>();

type WeaponSelectOption = SelectOption & { weapon: WeaponOption };

const remoteOptions = ref<WeaponSelectOption[]>([]);

function toOption(weapon: WeaponOption): WeaponSelectOption {
  return {
    label: `${weapon.name} - ${getWeaponTypeLabel(weapon.weaponType)}`,
    value: weapon.id,
    weapon,
  };
}

const options = computed(() => {
  const byId = new Map<string, WeaponSelectOption>();
  if (props.selectedWeapon) {
    byId.set(props.selectedWeapon.id, toOption(props.selectedWeapon));
  }
  for (const option of remoteOptions.value) {
    byId.set(String(option.value), option);
  }
  return [...byId.values()];
});

const { loading, search, debouncedSearch } = useGuardedRemoteSearch(
  (keyword) => props.source === "public"
    ? publicWeaponsApi.searchWeaponOptions({ keyword, pageSize: 30 })
    : adminWeaponsApi.searchWeaponOptions({ keyword, pageSize: 30 }),
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
  emit("update:selectedWeapon", nextValue ? ((option as WeaponSelectOption | null)?.weapon ?? null) : null);
}

watch(
  () => props.selectedWeapon,
  () => {
    if (props.selectedWeapon && !remoteOptions.value.some((option) => option.value === props.selectedWeapon?.id)) {
      remoteOptions.value = [toOption(props.selectedWeapon), ...remoteOptions.value];
    }
  },
);

onMounted(() => {
  void search();
});
</script>

<template>
  <n-select
    :clearable="clearable"
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
