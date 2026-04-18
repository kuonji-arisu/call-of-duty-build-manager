<script setup lang="ts">
import { computed, reactive, ref, watchEffect } from "vue";
import { storeToRefs } from "pinia";

import { useAppStore } from "../stores/app";
import { getGenerationOptions } from "../shared/utils/labels";
import { toSelectOptions } from "../shared/utils/naive";

const appStore = useAppStore();
const { settings } = storeToRefs(appStore);
const feedback = ref("");
const errorMessage = ref("");
const form = reactive({
  libraryTitle: "",
  homeGenerationFilter: "ALL",
});
const generationOptions = computed(() => toSelectOptions(getGenerationOptions()));

watchEffect(() => {
  form.libraryTitle =
    settings.value.find((item) => item.key === "libraryTitle")?.value ??
    "COD Build Manager";
  form.homeGenerationFilter =
    settings.value.find((item) => item.key === "homeGenerationFilter")?.value ?? "ALL";
});

async function saveSettings() {
  errorMessage.value = "";
  feedback.value = "";

  try {
    await appStore.saveSetting("libraryTitle", form.libraryTitle.trim() || "COD Build Manager");
    await appStore.saveSetting("homeGenerationFilter", form.homeGenerationFilter);
    feedback.value = "后台设置已保存。";
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  }
}
</script>

<template>
  <section class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="space-y-2">
        <p class="eyebrow-text">Admin · Settings</p>
        <h1 class="page-title">后台设置</h1>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-6">
        <div class="space-y-1">
          <p class="eyebrow-text">Preferences</p>
          <h3 class="text-xl font-semibold text-slate-50">基础偏好</h3>
        </div>

        <n-alert v-if="feedback" :bordered="false" type="success">
          {{ feedback }}
        </n-alert>
        <n-alert v-if="errorMessage" :bordered="false" type="error">
          {{ errorMessage }}
        </n-alert>

        <n-form label-placement="top">
          <div class="grid gap-4 lg:grid-cols-2">
            <n-form-item class="lg:col-span-2" label="库标题">
              <n-input v-model:value="form.libraryTitle" />
            </n-form-item>

            <n-form-item label="默认首页代际筛选">
              <n-select
                v-model:value="form.homeGenerationFilter"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationOptions]"
              />
            </n-form-item>
          </div>
        </n-form>

        <div class="flex flex-wrap gap-3">
          <n-button round type="primary" @click="saveSettings">
            保存设置
          </n-button>
        </div>
      </div>
    </n-card>
  </section>
</template>
