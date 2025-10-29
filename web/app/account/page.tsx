"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import {
  AUTH_EVENT,
  AuthUser,
  clearAuthSession,
  getAuthUser,
  loginWithCode,
  requestLoginCode,
  updateStoredUser
} from "../../lib/auth";
import { apiFetch } from "../../lib/api";
import { Badge, Button, Card, Divider, InfoRow, Section, palette, radii } from "../../components/ui";

export default function AccountPage() {
  const router = useRouter();
  const [phone, setPhone] = useState("");
  const [code, setCode] = useState("");
  const [user, setUser] = useState<AuthUser | null>(null);
  const [sending, setSending] = useState(false);
  const [loggingIn, setLoggingIn] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [remaining, setRemaining] = useState<number | null>(null);
  const [amount, setAmount] = useState("10");
  const [recharging, setRecharging] = useState(false);

  const syncUser = useCallback(() => {
    setUser(getAuthUser());
  }, []);

  const refreshPoints = useCallback(async () => {
    const current = getAuthUser();
    if (!current) {
      setRemaining(null);
      return;
    }
    const resp = await apiFetch("/points");
    if (!resp.ok) return;
    const data = await resp.json().catch(() => ({}));
    if (typeof data?.remaining === "number") {
      setRemaining(data.remaining);
      updateStoredUser({ remainingQuota: data.remaining });
    }
  }, []);

  useEffect(() => {
    syncUser();
    const handler = () => syncUser();
    window.addEventListener(AUTH_EVENT, handler);
    window.addEventListener("storage", handler);
    return () => {
      window.removeEventListener(AUTH_EVENT, handler);
      window.removeEventListener("storage", handler);
    };
  }, [syncUser]);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const search = new URLSearchParams(window.location.search);
    if (search.get("needLogin") === "1") {
      setMessage("请先登录，即可继续使用文案与成图功能。");
    }
  }, []);

  useEffect(() => {
    if (countdown <= 0) return;
    const timer = window.setTimeout(() => setCountdown((prev) => prev - 1), 1000);
    return () => window.clearTimeout(timer);
  }, [countdown]);

  useEffect(() => {
    refreshPoints();
  }, [user, refreshPoints]);

  const sendCode = async () => {
    setError(null);
    setMessage(null);
    if (!phone.trim()) {
      setError("请填写手机号");
      return;
    }
    setSending(true);
    try {
      await requestLoginCode(phone.trim());
      setMessage("验证码已发送，请在后端日志中查看（示例环境）。");
      setCountdown(60);
    } catch (err: any) {
      setError(err?.message || "验证码发送失败，请稍后再试");
    } finally {
      setSending(false);
    }
  };

  const login = async () => {
    setError(null);
    setMessage(null);
    if (!phone.trim() || !code.trim()) {
      setError("请填写手机号和验证码");
      return;
    }
    setLoggingIn(true);
    try {
      await loginWithCode(phone.trim(), code.trim());
      setMessage("登录成功，欢迎回来！正在跳转生成中心…");
      setCode("");
      syncUser();
      await refreshPoints();
      router.push("/upload");
    } catch (err: any) {
      setError(err?.message || "登录失败，请稍后再试");
    } finally {
      setLoggingIn(false);
    }
  };

  const logout = () => {
    clearAuthSession();
    setUser(null);
    setRemaining(null);
    setMessage("已退出登录。");
  };

  const handleRecharge = async () => {
    setError(null);
    setMessage(null);
    const amountNumber = parseFloat(amount);
    if (Number.isNaN(amountNumber) || amountNumber <= 0) {
      setError("请输入大于 0 的金额");
      return;
    }
    setRecharging(true);
    try {
      const resp = await apiFetch("/points/recharge", {
        method: "POST",
        body: JSON.stringify({ amountYuan: amountNumber })
      });
      if (resp.status === 401) {
        setError("请重新登录后再充值");
        return;
      }
      if (!resp.ok) {
        const text = await resp.text();
        throw new Error(text || "充值失败，请稍后再试");
      }
      const data = await resp.json();
      const remain = typeof data?.remaining === "number" ? data.remaining : null;
      if (remain !== null) {
        setRemaining(remain);
        updateStoredUser({ remainingQuota: remain });
      }
      setMessage(`充值成功，新增 ${data?.added ?? 0} 点，目前余额 ${remain ?? "-"}。`);
      setAmount("10");
    } catch (err: any) {
      setError(err?.message || "充值失败，请稍后再试");
    } finally {
      setRecharging(false);
    }
  };

  const remainingQuota = useMemo(() => remaining ?? user?.remainingQuota ?? 0, [remaining, user?.remainingQuota]);

  return (
    <div style={{ display: "grid", gap: 32 }}>
      <Section
        title="账户与点券中心"
        description="管理登录、点券与流程消耗。我们会在生成和成图时实时同步扣费，确保每个任务都可追踪。"
        action={<Badge tone={remainingQuota > 20 ? "success" : "warning"}>{remainingQuota > 20 ? "状态：充足" : "状态：建议充值"}</Badge>}
      >
        <Card style={{ padding: 28, display: "grid", gap: 24 }}>
          <div style={{ display: "grid", gap: 18, gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))" }}>
            <InfoRow label="账户状态" value={user ? "已登录" : "未登录"} hint={user ? `手机号：${user.phone}` : "登录后可同步点券余额"} />
            <InfoRow label="可用点券" value={`${remainingQuota} 点`} hint="生成文案扣 1 点 / 成图再扣 1 点（批量按素材数量计算）" />
            <InfoRow label="充值快捷入口" value="微信/支付宝转账" hint="内测阶段暂未接入支付，示例接口直接增加点券" />
          </div>
          <Divider />
          {user ? (
            <div style={{ display: "grid", gap: 24, gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))" }}>
              <Card style={{ padding: 20, borderRadius: radii.lg, display: "grid", gap: 16 }}>
                <h3 style={{ margin: 0 }}>点券充值</h3>
                <label style={{ display: "grid", gap: 8 }}>
                  <span style={{ fontSize: 13, color: palette.textMuted }}>充值金额（元）</span>
                  <input
                    type="number"
                    min={0.1}
                    step={0.1}
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    style={{
                      padding: 12,
                      borderRadius: radii.md,
                      border: `1px solid ${palette.border}`
                    }}
                  />
                </label>
                <span style={{ fontSize: 12, color: palette.textMuted }}>当前兑换比例：0.1 元 = 1 点券，充值后即时生效。</span>
                <div style={{ display: "flex", gap: 12 }}>
                  <Button onClick={handleRecharge} disabled={recharging}>
                    {recharging ? "充值中…" : "充值并刷新余额"}
                  </Button>
                  <Button variant="ghost" onClick={refreshPoints}>
                    手动同步余额
                  </Button>
                </div>
              </Card>

              <Card style={{ padding: 20, borderRadius: radii.lg, display: "grid", gap: 16 }}>
                <h3 style={{ margin: 0 }}>安全与会话</h3>
                <p style={{ margin: 0, color: palette.textMuted, lineHeight: 1.6 }}>
                  当前会话令牌与登录手机号绑定，冷启动或退出登录时会重新签发。若发现点券异常，可立即退出并重新登录。
                </p>
                <Button variant="ghost" onClick={logout}>
                  退出登录
                </Button>
              </Card>
            </div>
          ) : (
            <div style={{ display: "grid", gap: 24, gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))" }}>
              <Card style={{ padding: 20, borderRadius: radii.lg, display: "grid", gap: 16 }}>
                <h3 style={{ margin: 0 }}>快速登录</h3>
                <p style={{ margin: 0, color: palette.textMuted, lineHeight: 1.6 }}>
                  内测阶段采用验证码登陆，验证码会写入后端日志，供控制台查看。登录成功后会自动跳转到生成中心。
                </p>
                <label style={{ display: "grid", gap: 8 }}>
                  <span style={{ fontSize: 13, color: palette.textMuted }}>手机号</span>
                  <input
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="请输入手机号"
                    style={{
                      padding: 12,
                      borderRadius: radii.md,
                      border: `1px solid ${palette.border}`
                    }}
                  />
                </label>
                <label style={{ display: "grid", gap: 8 }}>
                  <span style={{ fontSize: 13, color: palette.textMuted }}>验证码</span>
                  <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
                    <input
                      value={code}
                      onChange={(e) => setCode(e.target.value)}
                      placeholder="请输入验证码"
                      style={{
                        flex: "1 1 160px",
                        padding: 12,
                        borderRadius: radii.md,
                        border: `1px solid ${palette.border}`
                      }}
                    />
                    <Button onClick={sendCode} disabled={sending || countdown > 0}>
                      {countdown > 0 ? `重新发送（${countdown}s）` : "获取验证码"}
                    </Button>
                  </div>
                </label>
                <Button onClick={login} disabled={loggingIn}>
                  {loggingIn ? "登录中…" : "立即登录"}
                </Button>
              </Card>

              <Card style={{ padding: 20, borderRadius: radii.lg, display: "grid", gap: 16 }}>
                <h3 style={{ margin: 0 }}>为什么要登录？</h3>
                <ul style={{ margin: 0, paddingLeft: 20, color: palette.textMuted, lineHeight: 1.6 }}>
                  <li>同步点券余额、生成历史与成图进度。</li>
                  <li>支持跨设备接力编辑，确保团队协作信息一致。</li>
                  <li>发生异常后可快速定位责任任务，避免重复扣费。</li>
                </ul>
              </Card>
            </div>
          )}
        </Card>
      </Section>

      {(message || error) && (
        <Card
          style={{
            borderColor: error ? palette.accent : palette.success,
            background: error ? "#fff5f5" : "#ecfeff",
            color: error ? palette.accent : palette.success,
            display: "flex",
            gap: 12,
            alignItems: "center"
          }}
        >
          <strong>{error ? "提醒" : "提示"}</strong>
          <span>{error ?? message}</span>
        </Card>
      )}
    </div>
  );
}
