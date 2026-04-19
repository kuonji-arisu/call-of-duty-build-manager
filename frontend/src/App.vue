<script setup lang="ts">
import { onMounted } from "vue";
import { storeToRefs } from "pinia";

import { darkTheme, themeOverrides } from "./naive";
import { useAppStore } from "./stores/app";
import { useAuthStore } from "./stores/auth";

const appStore = useAppStore();
const authStore = useAuthStore();
const { isReady, errorMessage } = storeToRefs(appStore);

onMounted(() => {
  void (async () => {
    await authStore.initialize();
    await appStore.bootstrap();
  })();
});
</script>

<template>
  <n-config-provider :theme="darkTheme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
      <div
        v-if="!isReady && !errorMessage"
        class="app-shell-bg flex min-h-screen items-center justify-center p-6"
      >
        <n-card :bordered="false" class="hero-panel glass-panel card-xl w-full max-w-xl">
          <div class="flex items-center gap-4">
            <n-spin size="large" />
            <div class="space-y-1">
              <p class="eyebrow-text">请稍候</p>
              <h1 class="text-2xl font-semibold text-slate-50">正在加载武器与配装数据</h1>
            </div>
          </div>
        </n-card>
      </div>

      <div
        v-else-if="errorMessage"
        class="app-shell-bg flex min-h-screen items-center justify-center p-6"
      >
        <n-card :bordered="false" class="glass-panel card-xl w-full max-w-2xl">
          <n-result description="启动时遇到了错误" status="error" title="初始化失败">
            <template #footer>
              <n-button round type="primary" @click="appStore.bootstrap()">
                重新加载
              </n-button>
            </template>
            <template #default>
              <div class="mx-auto max-w-xl text-sm text-slate-300">{{ errorMessage }}</div>
            </template>
          </n-result>
        </n-card>
      </div>

      <RouterView v-else />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>
