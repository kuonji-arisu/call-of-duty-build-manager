import { defineStore } from "pinia";

import { authApi } from "../api/auth";
import type { AuthUser, CaptchaPayload, LoginRequest } from "../shared/types";
import { clearAccessToken, getAccessToken, setAccessToken } from "./auth/token";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: getAccessToken(),
    currentUser: null as AuthUser | null,
    initialized: false,
  }),
  getters: {
    isLoggedIn(state) {
      return Boolean(state.token);
    },
    isAdmin(state) {
      return state.currentUser?.role === "ADMIN";
    },
  },
  actions: {
    async initialize() {
      const persistedToken = getAccessToken();
      this.token = persistedToken;

      if (!this.token) {
        this.currentUser = null;
        this.initialized = true;
        return;
      }

      if (this.initialized && this.currentUser) {
        return;
      }

      try {
        this.currentUser = await authApi.me();
      } catch {
        this.clearSession();
      } finally {
        this.initialized = true;
      }
    },
    async refreshCaptcha(): Promise<CaptchaPayload> {
      return authApi.createCaptcha();
    },
    async login(payload: LoginRequest) {
      const response = await authApi.login(payload);
      this.token = response.accessToken;
      setAccessToken(response.accessToken);
      this.currentUser = response.user;
      this.initialized = true;
      return response;
    },
    async ensureMe() {
      if (!this.token) {
        return null;
      }

      if (this.currentUser) {
        return this.currentUser;
      }

      this.currentUser = await authApi.me();
      return this.currentUser;
    },
    async logout() {
      try {
        if (this.token) {
          await authApi.logout();
        }
      } finally {
        this.clearSession();
      }
    },
    clearSession() {
      this.token = "";
      this.currentUser = null;
      clearAccessToken();
      this.initialized = true;
    },
  },
});
