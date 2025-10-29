"use client";

import { useEffect, useState } from "react";
import { apiFetch } from "../../lib/api";
import { Badge, Button, Card, Divider, Section, palette, radii } from "../../components/ui";

type HistoryRound = {
  id: number;
  instruction?: string;
  aiOutputs: string[];
  sequenceNo?: number;
  source?: string;
  createdAt?: string;
};

type HistoryGroup = {
  imageId: string;
  latestAt?: string | null;
  rounds: HistoryRound[];
};

export default function HistoryPage() {
  const [groups, setGroups] = useState<HistoryGroup[]>([]);
  const [totalRounds, setTotalRounds] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const resp = await apiFetch("/history");
      if (resp.status === 401) {
        setError("请先登录以查看历史生成记录。");
        return;
      }
      if (!resp.ok) {
        setError("历史记录加载失败，请稍后再试。");
        return;
      }
      const data = await resp.json();
      const list = Array.isArray(data.items) ? (data.items as HistoryGroup[]) : [];
      setGroups(
        list.map((group) => ({
          ...group,
          rounds: Array.isArray(group.rounds) ? group.rounds : []
        }))
      );
      setTotalRounds(typeof data.totalRounds === "number" ? data.totalRounds : 0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const deleteRound = async (id: number) => {
    await apiFetch(`/history/${id}`, { method: "DELETE" });
    load();
  };

  const copyRound = async (round: HistoryRound) => {
    const text = round.aiOutputs?.join("\n\n") || "";
    if (text) {
      await navigator.clipboard.writeText(text);
    }
  };

  return (
    <div style={{ display: "grid", gap: 32 }}>
      <Section
        title="生成档案库"
        description="系统会按图片维度聚合同一轮生成的所有内容，包含提示词、AI 输出与时间戳。你可以复用、删除或重新生成。"
        action={
          <Badge tone="neutral">
            {loading ? "加载中…" : `累计 ${totalRounds} 轮记录`}
          </Badge>
        }
      >
        <div style={{ display: "grid", gap: 20 }}>
          {error ? (
            <Card style={{ padding: 24, textAlign: "center", color: palette.textMuted }}>{error}</Card>
          ) : null}
          {!loading && !error && groups.length === 0 ? (
            <Card style={{ padding: 28, textAlign: "center", color: palette.textMuted }}>
              暂无历史记录，先到生成中心尝试一条文案吧。
            </Card>
          ) : null}

          {groups.map((group) => (
            <Card key={group.imageId} style={{ padding: 24, display: "grid", gap: 20, background: palette.surface }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 20, flexWrap: "wrap" }}>
                <div style={{ display: "grid", gap: 6 }}>
                  <span style={{ fontSize: 13, color: palette.textMuted }}>图片标识</span>
                  <a href={group.imageId} target="_blank" rel="noreferrer" style={{ color: palette.primary, wordBreak: "break-all" }}>
                    {group.imageId}
                  </a>
                </div>
                <div style={{ display: "grid", gap: 6, textAlign: "right" }}>
                  <span style={{ fontSize: 13, color: palette.textMuted }}>最近更新</span>
                  <strong>{group.latestAt ? new Date(group.latestAt).toLocaleString() : "未知时间"}</strong>
                </div>
              </div>

              <Divider />

              <div style={{ display: "grid", gap: 16 }}>
                {group.rounds.map((round) => (
                  <Card
                    key={round.id}
                    style={{
                      borderRadius: radii.lg,
                      border: `1px solid ${palette.border}`,
                      padding: 20,
                      display: "grid",
                      gap: 16
                    }}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 16, flexWrap: "wrap" }}>
                      <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                        <Badge tone="neutral">第 {round.sequenceNo ?? "-"} 轮</Badge>
                        <span style={{ fontSize: 13, color: palette.textMuted }}>
                          来源：{round.source?.toUpperCase() ?? "UNKNOWN"}
                        </span>
                      </div>
                      <span style={{ fontSize: 12, color: palette.textMuted }}>
                        {round.createdAt ? new Date(round.createdAt).toLocaleString() : "未知时间"}
                      </span>
                    </div>

                    <div>
                      <div style={{ color: palette.textMuted, fontSize: 12, marginBottom: 6 }}>用户指令</div>
                      <div style={{ whiteSpace: "pre-wrap", lineHeight: 1.7 }}>
                        {round.instruction || "（未提供指令）"}
                      </div>
                    </div>

                    <div>
                      <div style={{ color: palette.textMuted, fontSize: 12, marginBottom: 6 }}>AI 文案</div>
                      <ul style={{ margin: 0, paddingLeft: 18, lineHeight: 1.7 }}>
                        {round.aiOutputs && round.aiOutputs.length > 0 ? (
                          round.aiOutputs.map((text, idx) => <li key={idx}>{text}</li>)
                        ) : (
                          <li style={{ color: palette.textMuted }}>暂无输出</li>
                        )}
                      </ul>
                    </div>

                    <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
                      <Button variant="secondary" onClick={() => copyRound(round)}>
                        复制全部文案
                      </Button>
                      <Button variant="ghost" onClick={() => deleteRound(round.id)}>
                        删除记录
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            </Card>
          ))}
        </div>
      </Section>
    </div>
  );
}
