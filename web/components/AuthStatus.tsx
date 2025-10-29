"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { AUTH_EVENT, AuthUser, clearAuthSession, getAuthUser } from "../lib/auth";
import { Button, palette, radii } from "./ui";

function maskPhone(phone?: string) {
  if (!phone) return "";
  if (phone.length <= 7) return phone;
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}

export default function AuthStatus() {
  const [user, setUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    const update = () => setUser(getAuthUser());
    update();
    window.addEventListener(AUTH_EVENT, update);
    window.addEventListener("storage", update);
    return () => {
      window.removeEventListener(AUTH_EVENT, update);
      window.removeEventListener("storage", update);
    };
  }, []);

  if (!user) {
    return (
      <Link
        href="/account"
        style={{
          textDecoration: "none",
          padding: "8px 18px",
          borderRadius: radii.pill,
          border: `1px solid ${palette.border}`,
          color: palette.primary,
          fontWeight: 600,
          background: "#ffffff",
          boxShadow: "0 12px 22px rgba(37,99,235,0.12)"
        }}
      >
        登录 / 注册
      </Link>
    );
  }

  const logout = () => clearAuthSession();

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 12,
        padding: "8px 16px",
        borderRadius: radii.pill,
        border: `1px solid ${palette.border}`,
        background: "#ffffff",
        boxShadow: "0 12px 22px rgba(15, 23, 42, 0.08)"
      }}
    >
      <div style={{ display: "grid", gap: 2 }}>
        <span style={{ fontSize: 12, color: palette.textMuted }}>已登录</span>
        <strong style={{ fontSize: 14, color: palette.text }}>{maskPhone(user.phone)}</strong>
      </div>
      <Link href="/account" style={{ color: palette.primary, fontSize: 13, textDecoration: "none", fontWeight: 600 }}>
        账户中心
      </Link>
      <Button variant="ghost" onClick={logout} style={{ padding: "6px 14px" }}>
        退出
      </Button>
    </div>
  );
}
