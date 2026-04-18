import { defineStore } from "pinia";

import { authApi } from "../api/auth";
import type { AuthUser, CaptchaPayload, LoginRequest } from "../shared/types";
import { clearAccessToken, getAccessToken, setAccessToken } from "./auth/token";

const AUTH_SESSION_CLEARED_EVENT = "auth:session-cleared";
let sessionClearListenerRegistered = false;

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
    registerSessionClearListener() {
      if (sessionClearListenerRegistered) {
        return;
      }

      window.addEventListener(AUTH_SESSION_CLEARED_EVENT, () => {
        this.token = "";
        this.currentUser = null;
        this.initialized = true;
      });
      sessionClearListenerRegistered = true;
    },
    initialize() {
      this.registerSessionClearListener();
      const persistedToken = getAccessToken();
      const tokenChanged = persistedToken !== this.token;
      this.token = persistedToken;

      if (!this.token) {
        this.currentUser = null;
      } else if (tokenChanged) {
        this.currentUser = null;
      }

      this.initialized = true;
    },
    async verifySession() {
      this.initialize();

      if (!this.token) {
        return null;
      }

      try {
        this.currentUser = await authApi.me();
        return this.currentUser;
      } catch {
        this.clearSession();
        return null;
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
      this.initialize();

      if (!this.token) {
        return null;
      }

      if (this.currentUser) {
        return this.currentUser;
      }

      return this.verifySession();
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
