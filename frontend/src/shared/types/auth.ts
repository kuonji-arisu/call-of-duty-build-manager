import type { UserRole } from "./common";

export interface AuthUser {
  id: string;
  username: string;
  displayName: string;
  role: UserRole;
}

export interface CaptchaPayload {
  captchaId: string;
  imageData: string;
  expiresInSeconds: number;
}

export interface LoginRequest {
  username: string;
  password: string;
  captchaId: string;
  captchaCode: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: AuthUser;
}
