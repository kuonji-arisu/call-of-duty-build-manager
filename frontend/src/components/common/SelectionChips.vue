<script setup lang="ts">
import type { OptionItem } from "../../shared/types";

const props = defineProps<{
  modelValue: string[];
  options: OptionItem[];
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string[]];
}>();

function toggle(value: string) {
  if (props.modelValue.includes(value)) {
    emit(
      "update:modelValue",
      props.modelValue.filter((item) => item !== value),
    );
    return;
  }

  emit("update:modelValue", [...props.modelValue, value]);
}
</script>

<template>
  <div class="flex flex-wrap gap-2.5">
    <button
      v-for="option in options"
      :key="option.value"
      class="rounded-full border px-3 py-1.5 text-sm transition"
      :class="
        modelValue.includes(option.value)
          ? 'border-sky-400/30 bg-sky-500/16 text-sky-100'
          : 'border-white/8 bg-white/4 text-slate-200 hover:bg-white/8'
      "
      type="button"
      @click="toggle(option.value)"
    >
      {{ option.label }}
    </button>
  </div>
</template>
