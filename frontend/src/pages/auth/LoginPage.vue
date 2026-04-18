<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

import { useAuthStore } from "../../stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  username: "",
  password: "",
  captchaId: "",
  captchaCode: "",
  imageData: "",
});
const isSubmitting = ref(false);
const errorMessage = ref("");

async function refreshCaptcha() {
  errorMessage.value = "";
  const captcha = await authStore.refreshCaptcha();
  form.captchaId = captcha.captchaId;
  form.imageData = captcha.imageData;
  form.captchaCode = "";
}

async function submit() {
  isSubmitting.value = true;
  errorMessage.value = "";

  try {
    await authStore.login({
      username: form.username.trim(),
      password: form.password,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode.trim(),
    });

    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/admin/weapons";
    await router.push(redirect);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
    await refreshCaptcha();
  } finally {
    isSubmitting.value = false;
  }
}

onMounted(() => {
  void refreshCaptcha();
});
</script>

<template>
  <div class="app-shell-bg flex min-h-screen items-center justify-center p-6">
    <div class="grid w-full max-w-6xl gap-6 xl:grid-cols-[minmax(320px,1fr)_520px]">
      <n-card :bordered="false" class="hero-panel glass-panel card-xl hidden xl:block">
        <div class="flex h-full flex-col justify-between gap-8">
          <div class="space-y-4">
            <p class="eyebrow-text">后台管理</p>
            <h1 class="text-4xl font-semibold leading-tight text-slate-50">进入后台管理</h1>
            <p class="max-w-md text-base leading-7 text-slate-300">
              用统一的后台入口维护武器、配件、属性词条和推荐配装。
            </p>
          </div>

          <div class="grid gap-3 md:grid-cols-3">
            <div class="rounded-3xl border border-white/8 bg-white/4 p-4">
              <p class="text-sm text-slate-400">模块</p>
              <p class="mt-2 text-lg font-semibold text-slate-100">武器与配件</p>
            </div>
            <div class="rounded-3xl border border-white/8 bg-white/4 p-4">
              <p class="text-sm text-slate-400">维护</p>
              <p class="mt-2 text-lg font-semibold text-slate-100">属性词条</p>
            </div>
            <div class="rounded-3xl border border-white/8 bg-white/4 p-4">
              <p class="text-sm text-slate-400">内容</p>
              <p class="mt-2 text-lg font-semibold text-slate-100">推荐配装</p>
            </div>
          </div>
        </div>
      </n-card>

      <n-card :bordered="false" class="glass-panel card-xl">
        <div class="space-y-6">
          <div class="space-y-2">
            <p class="eyebrow-text">登录</p>
            <h1 class="text-3xl font-semibold tracking-tight text-slate-50">后台登录</h1>
            <p class="text-sm text-slate-400">输入账号、密码和验证码后进入后台。</p>
          </div>

          <n-alert v-if="errorMessage" :bordered="false" type="error">
            {{ errorMessage }}
          </n-alert>

          <n-form label-placement="top">
            <div class="grid gap-4">
              <n-form-item label="用户名">
                <n-input
                  v-model:value="form.username"
                  autocomplete="username"
                  placeholder="请输入管理员账号"
                />
              </n-form-item>

              <n-form-item label="密码">
                <n-input
                  v-model:value="form.password"
                  autocomplete="current-password"
                  placeholder="请输入密码"
                  show-password-on="click"
                  type="password"
                />
              </n-form-item>

              <n-form-item label="验证码">
                <div class="flex flex-col gap-3 sm:flex-row">
                  <n-input
                    v-model:value="form.captchaCode"
                    class="flex-1"
                    maxlength="8"
                    placeholder="请输入验证码"
                  />
                  <n-image
                    v-if="form.imageData"
                    :preview-disabled="true"
                    :src="form.imageData"
                    class="overflow-hidden rounded-2xl border border-white/8"
                    width="132"
                  />
                  <n-button secondary type="info" @click="refreshCaptcha">
                    刷新验证码
                  </n-button>
                </div>
              </n-form-item>
            </div>
          </n-form>

          <div class="flex flex-wrap gap-3">
            <n-button :loading="isSubmitting" round type="primary" @click="submit">
              {{ isSubmitting ? "登录中..." : "登录" }}
            </n-button>
            <RouterLink to="/">
              <n-button quaternary round>返回公开页面</n-button>
            </RouterLink>
          </div>
        </div>
      </n-card>
    </div>
  </div>
</template>
