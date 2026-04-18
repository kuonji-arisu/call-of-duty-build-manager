<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
}>();
</script>

<template>
  <n-empty
    :description="description"
    class="glass-panel rounded-[28px] border border-white/8 px-6 py-10"
    size="large"
  >
    <template #icon>
      <div class="flex h-8 w-full items-center justify-center">
        <div class="h-3 w-3 rounded-full bg-sky-400 shadow-[0_0_24px_rgba(105,183,244,0.55)]" />
      </div>
    </template>
    <template #default>
      <div class="mt-3 text-lg font-semibold text-slate-50">{{ title }}</div>
    </template>
    <template #extra>
      <div class="mt-4">
        <slot />
      </div>
    </template>
  </n-empty>
</template>
