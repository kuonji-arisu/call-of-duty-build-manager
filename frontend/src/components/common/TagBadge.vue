<script setup lang="ts">
import { computed } from "vue";
import type { TagProps } from "naive-ui";

const props = withDefaults(
  defineProps<{
    tone?: "default" | "accent" | "danger" | "muted";
  }>(),
  {
    tone: "default",
  },
);

const tagType = computed<TagProps["type"]>(() => {
  if (props.tone === "accent") {
    return "info";
  }

  if (props.tone === "danger") {
    return "error";
  }

  return "default";
});

const tagColor = computed<TagProps["color"] | undefined>(() => {
  if (props.tone === "muted") {
    return {
      color: "rgba(255, 255, 255, 0.06)",
      borderColor: "rgba(255, 255, 255, 0.1)",
      textColor: "#cbd5e1",
    };
  }

  if (props.tone === "default") {
    return {
      color: "rgba(105, 183, 244, 0.12)",
      borderColor: "rgba(105, 183, 244, 0.18)",
      textColor: "#bae6fd",
    };
  }

  return undefined;
});
</script>

<template>
  <n-tag
    :bordered="false"
    :color="tagColor"
    :type="tagType"
    round
    size="small"
  >
    <slot />
  </n-tag>
</template>
