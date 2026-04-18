import router from "../router";
import { clearAccessToken, getAccessToken } from "../stores/auth/token";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, "") ??
  "http://localhost:8080/api";

interface RequestOptions {
  auth?: boolean;
}

interface ApiErrorBody {
  message?: unknown;
}

function parseResponseBody(raw: string, contentType: string | null): unknown {
  if (!raw) {
    return null;
  }

  if (!contentType?.toLowerCase().includes("application/json")) {
    return raw;
  }

  try {
    return JSON.parse(raw) as unknown;
  } catch {
    return raw;
  }
}

function errorMessageFromBody(data: unknown, response: Response) {
  if (typeof data === "object" && data && "message" in data) {
    const message = (data as ApiErrorBody).message;
    if (typeof message === "string" && message.trim()) {
      return message;
    }
  }

  if (typeof data === "string" && data.trim()) {
    return data.trim();
  }

  return `${response.status} ${response.statusText}`;
}

export async function request<T>(
  path: string,
  init?: RequestInit,
  options: RequestOptions = {},
): Promise<T> {
  const token = options.auth ? getAccessToken() : "";
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  const raw = await response.text();
  const data = parseResponseBody(raw, response.headers.get("content-type"));

  if (!response.ok) {
    if (response.status === 401 && options.auth) {
      clearAccessToken();
      const redirect = `${window.location.pathname}${window.location.search}`;
      await router.push({
        path: "/login",
        query: { redirect },
      });
    }

    throw new Error(errorMessageFromBody(data, response));
  }

  return data as T;
}
