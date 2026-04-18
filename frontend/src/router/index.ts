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
        { path: "my-builds/:id", component: () => import("../pages/LocalBuildDetailPage.vue") },
        { path: "my-builds", component: () => import("../pages/LocalBuildsPage.vue") },
        { path: "admin/settings", component: () => import("../pages/admin/SettingsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/weapons", component: () => import("../pages/admin/AdminWeaponsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/attachments", component: () => import("../pages/admin/AdminAttachmentsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/weapon-attachment-bindings", component: () => import("../pages/admin/AdminWeaponAttachmentBindingsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/attachment-effects", component: () => import("../pages/admin/AdminAttachmentEffectDefinitionsPage.vue"), meta: { requiresAdmin: true } },
        { path: "admin/builds", component: () => import("../pages/admin/AdminBuildsPage.vue"), meta: { requiresAdmin: true } },
      ],
    },
  ],
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia);
  authStore.initialize();

  if (to.meta.guestOnly) {
    if (!authStore.isLoggedIn) {
      return true;
    }

    const currentUser = await authStore.ensureMe();
    return currentUser ? "/" : true;
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
  if (!currentUser) {
    return {
      path: "/login",
      query: { redirect: to.fullPath },
    };
  }

  if (currentUser.role !== "ADMIN") {
    return "/";
  }

  return true;
});

export default router;
