<script setup lang="ts">
import { computed } from "vue";
import { storeToRefs } from "pinia";
import { useRoute, useRouter } from "vue-router";

import { useAppStore } from "../stores/app";
import { useAuthStore } from "../stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const appStore = useAppStore();
const { libraryTitle } = storeToRefs(appStore);

const sections = computed(() => [
  {
    title: "浏览",
    items: [
      { label: "武器库", to: "/" },
      { label: "我的配装", to: "/my-builds" },
    ],
  },
  ...(authStore.isAdmin
    ? [{
        title: "后台管理",
        items: [
          { label: "武器管理", to: "/admin/weapons" },
          { label: "配件管理", to: "/admin/attachments" },
          { label: "武器配件绑定", to: "/admin/weapon-attachment-bindings" },
          { label: "属性词条", to: "/admin/attachment-effects" },
          { label: "推荐配装", to: "/admin/builds" },
          { label: "后台设置", to: "/admin/settings" },
        ],
      }]
    : []),
]);

function isActive(path: string) {
  if (path === "/") {
    return route.path === path;
  }

  return route.path.startsWith(path);
}

function linkClass(path: string) {
  return [
    "block rounded-2xl px-4 py-3 text-sm font-medium transition",
    isActive(path)
      ? "bg-sky-500/16 text-sky-100 shadow-[inset_0_0_0_1px_rgba(105,183,244,0.22)]"
      : "text-slate-200 hover:bg-white/6 hover:text-white",
  ];
}

async function logout() {
  await authStore.logout();
  await router.push("/");
}
</script>

<template>
  <aside class="sticky top-4 self-start">
    <n-card :bordered="false" class="glass-panel card-xl">
      <n-space vertical :size="28">
        <div class="space-y-2">
          <p class="eyebrow-text">导航</p>
          <h1 class="text-2xl font-semibold tracking-tight text-slate-50">{{ libraryTitle }}</h1>
          <p class="muted-copy text-sm">武器、配件与配装</p>
        </div>

        <div class="space-y-6">
          <section v-for="section in sections" :key="section.title" class="space-y-2">
            <p class="eyebrow-text">{{ section.title }}</p>
            <div class="space-y-2">
              <RouterLink
                v-for="item in section.items"
                :key="item.to"
                :class="linkClass(item.to)"
                :to="item.to"
              >
                {{ item.label }}
              </RouterLink>
            </div>
          </section>

          <section class="space-y-2">
            <p class="eyebrow-text">账号</p>
            <RouterLink v-if="!authStore.isLoggedIn" :class="linkClass('/login')" to="/login">
              管理员登录
            </RouterLink>
            <button
              v-else
              class="block w-full rounded-2xl px-4 py-3 text-left text-sm font-medium text-slate-200 transition hover:bg-rose-500/12 hover:text-rose-100"
              type="button"
              @click="logout"
            >
              退出登录
            </button>
          </section>
        </div>
      </n-space>
    </n-card>
  </aside>
</template>
