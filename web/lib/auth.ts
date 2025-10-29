"use client";

import { API_BASE } from "./config";

export const AUTH_EVENT = "aipyq-auth-changed";
const TOKEN_KEY = "aipyq_auth_token";
const USER_KEY = "aipyq_auth_user";

const isBrowser = () => typeof window !== "undefined";

export interface AuthUser {
  id: string;
  phone: string;
  planId?: string | null;
  remainingQuota?: number | null;
}

export interface LoginResponse {
  token: string;
  user: AuthUser;
  message?: string;
}

function notify() {
  if (isBrowser()) {
    window.dispatchEvent(new Event(AUTH_EVENT));
  }
}

export function getAuthToken(): string {
  if (!isBrowser()) return "";
  return window.localStorage.getItem(TOKEN_KEY) || "";
}

export function getAuthUser(): AuthUser | null {
  if (!isBrowser()) return null;
  const raw = window.localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function setAuthSession(token: string, user: AuthUser) {
  if (!isBrowser()) return;
  window.localStorage.setItem(TOKEN_KEY, token);
  window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  notify();
}

export function updateStoredUser(partial: Partial<AuthUser>) {
  if (!isBrowser()) return;
  const current = getAuthUser();
  if (!current) return;
  const next = { ...current, ...partial };
  window.localStorage.setItem(USER_KEY, JSON.stringify(next));
  notify();
}

export function clearAuthSession() {
  if (!isBrowser()) return;
  window.localStorage.removeItem(TOKEN_KEY);
  window.localStorage.removeItem(USER_KEY);
  notify();
}

export async function requestLoginCode(phone: string): Promise<void> {
  const resp = await fetch(`${API_BASE}/auth/send-code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ phone })
  });
  if (!resp.ok) {
    throw new Error("验证码发送失败，请稍后再试");
  }
}

export async function loginWithCode(phone: string, code: string): Promise<LoginResponse> {
  const resp = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ phone, code })
  });
  const data = await resp.json().catch(() => ({}));
  if (!resp.ok) {
    throw new Error((data as LoginResponse)?.message || "登录失败，请检查验证码");
  }
  const loginData = data as LoginResponse;
  if (loginData.token && loginData.user) {
    setAuthSession(loginData.token, loginData.user);
  }
  return loginData;
}
