"use client";

import { API_BASE, apiBase as baseFn } from "./config";
import { getClientId } from "./clientId";
import { clearAuthSession, getAuthToken } from "./auth";

export function apiBase(): string {
  return baseFn();
}

export async function apiFetch(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers || {});
  const cid = getClientId();
  if (cid) {
    headers.set("X-Client-Id", cid);
  }
  const token = getAuthToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  if (!headers.has("Content-Type") && !(init.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (response.status === 401 && typeof window !== "undefined") {
    clearAuthSession();
    if (!window.location.pathname.startsWith("/account")) {
      window.location.href = "/account?needLogin=1";
    }
  }
  return response;
}
