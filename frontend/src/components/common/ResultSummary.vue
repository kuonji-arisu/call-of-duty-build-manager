<script setup lang="ts">
defineProps<{
  total: number;
  page: number;
  pageSize: number;
  loading?: boolean;
  label?: string;
}>();
</script>

<template>
  <div class="flex flex-wrap items-center justify-between gap-3 text-sm text-slate-400">
    <p>
      <span v-if="loading">正在加载</span>
      <span v-else>
        共 {{ total }} 条{{ label ?? "结果" }}
        <template v-if="total">
          ，当前第 {{ page }} 页，每页 {{ pageSize }} 条
        </template>
      </span>
    </p>
    <slot />
  </div>
</template>
