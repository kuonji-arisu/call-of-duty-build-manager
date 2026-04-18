import router from "../router";
import { clearAccessToken, getAccessToken } from "../stores/auth/token";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, "") ??
  "http://localhost:8080/api";

interface RequestOptions {
  auth?: boolean;
}

export async function request<T>(
  path: string,
  init?: RequestInit,
  options: RequestOptions = {},
): Promise<T> {
  const token = options.auth ? getAccessToken() : "";
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init?.headers,
    },
  });

  const raw = await response.text();
  const data = raw ? (JSON.parse(raw) as unknown) : null;

  if (!response.ok) {
    if (response.status === 401 && options.auth) {
      clearAccessToken();
      const redirect = `${window.location.pathname}${window.location.search}`;
      await router.push({
        path: "/login",
        query: { redirect },
      });
    }

    const message =
      typeof data === "object" && data && "message" in data
        ? String((data as { message?: unknown }).message)
        : `${response.status} ${response.statusText}`;
    throw new Error(message);
  }

  return data as T;
}
