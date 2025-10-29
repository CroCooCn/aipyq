import Link from "next/link";
import AuthStatus from "../components/AuthStatus";
import { palette, radii } from "../components/ui";

export const metadata = {
  title: "朋友圈文案助手",
  description: "一站式完成朋友圈文案、成图与素材管理"
};

const NAV_ITEMS: { href: string; label: string }[] = [
  { href: "/upload", label: "生成中心" },
  { href: "/templates", label: "灵感模板" },
  { href: "/history", label: "历史档案" },
  { href: "/account", label: "账户与点券" }
];

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body
        style={{
          margin: 0,
          background: palette.background,
          color: palette.text,
          fontFamily: '"Inter", "PingFang SC", "Microsoft YaHei", sans-serif',
          WebkitFontSmoothing: "antialiased"
        }}
      >
        <header
          style={{
            background: "linear-gradient(120deg, #f6f8ff 0%, #e7eeff 50%, #d5e3ff 100%)",
            borderBottom: `1px solid ${palette.border}`,
            padding: "18px 0 16px",
            position: "sticky",
            top: 0,
            zIndex: 10,
            boxShadow: "0 14px 34px rgba(96, 124, 212, 0.16)"
          }}
        >
          <div
            style={{
              width: "min(1080px, calc(100% - 64px))",
              margin: "0 auto",
              display: "flex",
              flexDirection: "column",
              gap: 14
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", gap: 20, alignItems: "center", flexWrap: "wrap" }}>
              <Link
                href="/"
                style={{
                  textDecoration: "none",
                  display: "flex",
                  alignItems: "center",
                  gap: 12,
                  color: palette.text
                }}
              >
                <div
                  style={{
                    height: 38,
                    width: 38,
                    borderRadius: radii.lg,
                    background: "#eef3ff",
                    display: "grid",
                    placeItems: "center",
                    fontWeight: 700,
                    fontSize: 18,
                    color: palette.primary
                  }}
                >
                  PY
                </div>
                <div style={{ display: "grid", lineHeight: 1.2 }}>
                  <strong style={{ fontSize: 19 }}>朋友圈文案助手</strong>
                  <span style={{ fontSize: 12, color: palette.textMuted }}>文案·成图·素材，一条链路完成</span>
                </div>
              </Link>
              <AuthStatus />
            </div>
            <nav style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
              {NAV_ITEMS.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  style={{
                    padding: "8px 16px",
                    borderRadius: radii.pill,
                    background: "rgba(37,99,235,0.08)",
                    border: `1px solid rgba(37,99,235,0.16)`,
                    color: palette.primary,
                    textDecoration: "none",
                    fontSize: 14,
                    fontWeight: 500
                  }}
                >
                  {item.label}
                </Link>
              ))}
            </nav>
          </div>
        </header>

        <main
          style={{
            width: "min(1080px, calc(100% - 64px))",
            margin: "32px auto 96px",
            display: "grid",
            gap: 32
          }}
        >
          {children}
        </main>

        <footer
          style={{
            borderTop: `1px solid ${palette.borderStrong}`,
            padding: "24px 0",
            background: palette.surface,
            color: palette.textMuted
          }}
        >
          <div
            style={{
              width: "min(1080px, calc(100% - 64px))",
              margin: "0 auto",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              flexWrap: "wrap",
              gap: 16,
              fontSize: 13
            }}
          >
            <span>© {new Date().getFullYear()} 朋友圈文案助手 · 为内容创作者打造</span>
            <div style={{ display: "flex", gap: 16 }}>
              {NAV_ITEMS.map((item) => (
                <Link key={item.href} href={item.href} style={{ color: palette.textMuted, textDecoration: "none" }}>
                  {item.label}
                </Link>
              ))}
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
