import Link from "next/link";
import { Badge, Button, Card, Divider, Section, StatCard, palette, radii } from "../components/ui";

const NAV_CARDS = [
  {
    href: "/upload",
    label: "生成中心",
    description: "上传图片、选择语气，批量产出朋友圈文案与成图。"
  },
  {
    href: "/templates",
    label: "灵感模板",
    description: "行业语气与结构拆解，一键带入生成流程后再微调。"
  },
  {
    href: "/history",
    label: "历史档案",
    description: "按图片聚合历次文案与成图，支持复用、下载与备注。"
  },
  {
    href: "/account",
    label: "账户与点券",
    description: "查看点券余额、充值记录与登录安全信息。"
  }
];

const FLOW_FEATURES = [
  {
    title: "多图同步文案",
    description: "一次上传多张素材，生成一套情绪统一的朋友圈文案，告别逐条复制粘贴。"
  },
  {
    title: "一键成图流水线",
    description: "文案确定后自动触发成图任务，可随时查看排队与完成情况。"
  },
  {
    title: "灵感模板矩阵",
    description: "覆盖日常记录、品牌上新、旅拍故事、能量打卡四大场景，高完成度示例随取随用。"
  }
];

export default function Page() {
  return (
    <div style={{ display: "grid", gap: 40 }}>
      <Card
        highlight
        style={{
          padding: 40,
          display: "grid",
          gap: 28,
          background: "linear-gradient(135deg, #ffffff 0%, #f4f7ff 60%, #e5eeff 100%)"
        }}
      >
        <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
          <Badge tone="accent">创作者专注包</Badge>
          <span style={{ fontSize: 13, color: palette.textMuted }}>Stories · Copywriting · Visuals in one flow</span>
        </div>
        <div style={{ display: "grid", gap: 16 }}>
          <h1 style={{ margin: 0, fontSize: 36, color: palette.text, lineHeight: 1.25 }}>
            朋友圈更新不再眉头紧锁：灵感、文案、成图，一条链路完成
          </h1>
          <p style={{ margin: 0, color: palette.textMuted, fontSize: 16, lineHeight: 1.7, maxWidth: 640 }}>
            上传素材、选择语气、确认句式，系统会同时准备批量文案、配图和历史档案。你只需挑选喜欢的版本，剩余的重复工作交给工具。
          </p>
        </div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
          <Link href="/upload">
            <Button style={{ paddingInline: 28, fontSize: 15 }}>马上生成一条朋友圈</Button>
          </Link>
          <Link href="/templates">
            <Button variant="ghost" style={{ fontSize: 15, borderColor: palette.borderStrong }}>先看看灵感模板</Button>
          </Link>
        </div>
        <div style={{ display: "grid", gap: 16, gridTemplateColumns: "repeat(auto-fit, minmax(140px, 1fr))" }}>
          <StatCard label="平均生成时间" value="18 秒" delta="较手动编辑节省 78%" />
          <StatCard label="一键成图成功率" value="96%" delta="失败任务自动重试补偿" />
          <StatCard label="创作者满意度" value="4.8 / 5" delta="近三个月有效反馈" />
        </div>
      </Card>

      <Section
        title="为社交内容打造的三段式流程"
        description="从需求梳理到成果交付，每一步都可回滚、复用、追踪。"
      >
        <div style={{ display: "grid", gap: 20, gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))" }}>
          {FLOW_FEATURES.map((feature) => (
            <Card key={feature.title} style={{ gap: 12, display: "grid", borderRadius: radii.xl, padding: 28 }}>
              <h3 style={{ margin: 0, fontSize: 18 }}>{feature.title}</h3>
              <p style={{ margin: 0, color: palette.textMuted, lineHeight: 1.6 }}>{feature.description}</p>
              <Divider />
              <Link href="/upload" style={{ color: palette.primary, fontWeight: 600, textDecoration: "none", fontSize: 14 }}>
                了解更多 &rarr;
              </Link>
            </Card>
          ))}
        </div>
      </Section>

      <Section title="快速导航" description="所有高频功能集中在同一条导航线上，减少无意义的页面跳转。">
        <div
          style={{
            display: "grid",
            gap: 16,
            gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))"
          }}
        >
          {NAV_CARDS.map((item) => (
            <Card key={item.href} style={{ padding: 24, display: "grid", gap: 10, borderRadius: radii.lg }}>
              <span style={{ fontSize: 12, letterSpacing: 0.4, color: palette.textMuted }}>入口</span>
              <strong style={{ fontSize: 20 }}>{item.label}</strong>
              <p style={{ margin: 0, fontSize: 13, color: palette.textMuted, lineHeight: 1.6 }}>{item.description}</p>
              <Link href={item.href} style={{ marginTop: 6, color: palette.primary, fontWeight: 600, textDecoration: "none", fontSize: 14 }}>
                立即前往
              </Link>
            </Card>
          ))}
        </div>
      </Section>
    </div>
  );
}
