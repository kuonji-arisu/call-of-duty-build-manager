<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";

import BuildDetailView from "../components/builds/BuildDetailView.vue";
import EmptyState from "../components/common/EmptyState.vue";
import { publicBuildsApi } from "../api/public/builds";
import { useLatestRequest } from "../composables/useLatestRequest";
import type { BuildDetail } from "../shared/types";

const route = useRoute();
const detail = ref<BuildDetail | null>(null);
const loading = ref(false);
const errorMessage = ref("");
const detailRequest = useLatestRequest();

const weaponId = computed(() => detail.value?.weapon.id ?? "");

async function loadDetail(id: string) {
  const request = detailRequest.next();
  loading.value = true;
  errorMessage.value = "";
  detail.value = null;

  try {
    const result = await publicBuildsApi.getBuildDetail(id);
    if (!request.isLatest()) {
      return;
    }
    detail.value = result;
  } catch (error) {
    if (request.isLatest()) {
      detail.value = null;
      errorMessage.value = error instanceof Error ? error.message : String(error);
    }
  } finally {
    if (request.isLatest()) {
      loading.value = false;
    }
  }
}

watch(
  () => route.params.id,
  (id) => {
    void loadDetail(String(id));
  },
  { immediate: true },
);
</script>

<template>
  <EmptyState
    v-if="!detail && !loading"
    title="找不到配装"
    :description="errorMessage || '这个推荐配装可能已经被删除，或者链接中的 ID 已失效。'"
  >
    <RouterLink class="secondary-button" to="/">返回武器库</RouterLink>
  </EmptyState>

  <BuildDetailView
    v-else-if="detail"
    :detail="detail"
    eyebrow="Build Detail"
    :back-link="`/weapons/${weaponId}`"
    back-text="返回武器详情"
  />
</template>
