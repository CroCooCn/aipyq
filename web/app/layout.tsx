export const metadata = { title: '每日朋友圈助手', description: '生成有灵魂的朋友圈文案与成图' };

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body style={{ fontFamily: 'system-ui, -apple-system, Segoe UI, Roboto' }}>
        <header style={{ padding: 16, borderBottom: '1px solid #eee' }}>
          <strong>每日朋友圈助手</strong>
        </header>
        <main style={{ padding: 16 }}>{children}</main>
      </body>
    </html>
  );
}

