<script setup lang="ts">
withDefaults(
  defineProps<{
    page: number;
    pageSize: number;
    total: number;
    disabled?: boolean;
    pageSizes?: number[];
  }>(),
  {
    disabled: false,
    pageSizes: () => [10, 20, 40, 60],
  },
);

const emit = defineEmits<{
  "update:page": [value: number];
  "update:pageSize": [value: number];
}>();
</script>

<template>
  <div class="flex justify-center pt-2">
    <n-pagination
      :disabled="disabled"
      :item-count="total"
      :page="page"
      :page-size="pageSize"
      :page-sizes="pageSizes"
      show-size-picker
      @update:page="emit('update:page', $event)"
      @update:page-size="emit('update:pageSize', $event)"
    />
  </div>
</template>
