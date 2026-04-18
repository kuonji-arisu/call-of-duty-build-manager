const AUTH_TOKEN_KEY = "cod-build-manager:access-token";

export function getAccessToken(): string {
  return localStorage.getItem(AUTH_TOKEN_KEY) ?? "";
}

export function setAccessToken(token: string) {
  localStorage.setItem(AUTH_TOKEN_KEY, token);
}

export function clearAccessToken() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
}
