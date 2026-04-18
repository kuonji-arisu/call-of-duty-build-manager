import { createRouter, createWebHistory } from "vue-router";

import { pinia } from "../pinia";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      component: () => import("../pages/auth/LoginPage.vue"),
      meta: { guestOnly: true },
    },
    {
      path: "/",
      component: () => import("../layouts/AppShell.vue"),
      children: [
        { path: "", component: () => import("../pages/WeaponListPage.vue") },
        { path: "weapons/:id", component: () => import("../pages/WeaponDetailPage.vue") },
        { path: "builds/:id", component: () => import("../pages/BuildDetailPage.vue") },
        { path: "my-builds/:id", component: () => import("../pages/user/UserBuildDetailPage.vue") },
        { path: "my-builds", component: () => import("../pages/user/UserBuildsPage.vue") },
        { path: "admin/settings", component: () => import("../pages/SettingsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/weapons", component: () => import("../pages/AdminWeaponsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/attachments", component: () => import("../pages/AdminAttachmentsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/weapon-attachment-bindings", component: () => import("../pages/AdminWeaponAttachmentBindingsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/attachment-effects", component: () => import("../pages/AdminAttachmentEffectDefinitionsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/builds", component: () => import("../pages/AdminBuildsPage.vue"), meta: { requiresAdmin: true } },
      ],
    },
  ],
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia);
  await authStore.initialize();

  if (to.meta.guestOnly && authStore.isLoggedIn) {
    return "/";
  }

  if (!to.meta.requiresAdmin) {
    return true;
  }

  if (!authStore.isLoggedIn) {
    return {
      path: "/login",
      query: { redirect: to.fullPath },
    };
  }

  const currentUser = await authStore.ensureMe();
  if (currentUser?.role !== "ADMIN") {
    return "/";
  }

  return true;
});

export default router;
