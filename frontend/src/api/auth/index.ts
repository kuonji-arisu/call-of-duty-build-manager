import { request } from "../client";
import type {
  AuthUser,
  CaptchaPayload,
  LoginRequest,
  LoginResponse,
} from "../../shared/types";

export const authApi = {
  createCaptcha: () =>
    request<CaptchaPayload>("/auth/captcha", { method: "POST" }),
  login: (payload: LoginRequest) =>
    request<LoginResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  logout: () =>
    request<{ message: string }>("/auth/logout", { method: "POST" }, { auth: true }),
  me: () => request<AuthUser>("/auth/me", undefined, { auth: true }),
};
