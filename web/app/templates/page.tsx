"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { emitTemplateSelection } from "../../lib/templates";
import { Badge, Button, Card, Section, palette, radii } from "../../components/ui";

type TemplateItem = {
  id: string;
  title: string;
  mood: string;
  summary: string;
  styleInstruction: string;
  structure: string[];
  sample: string;
};

const templates: TemplateItem[] = [
  {
    id: "daily-soft",
    title: "温柔日常 · Light Mood",
    mood: "适合生活记录、宠物、咖啡馆等轻松场景",
    summary: "用细节取胜，重点传递当下的感受与温度。",
    styleInstruction:
      "语气温暖，开头用细节描写画面（光线、声音、温度），中段表达内心感受，结尾给出轻柔的互动邀请或祝福，emoji 不超过 2 个。",
    structure: [
      "画面细节起笔：光影/气味/声音",
      "心情升华：为什么这一刻值得记录",
      "温柔收尾：留一个问题或祝福，增强互动"
    ],
    sample:
      "窗边的柠檬光慢慢溢进来，猫咪窝在腿边呼噜，手里的咖啡刚好 62°C。\n这就是想努力过好每一天的理由吧，所有小意外都变得值得。\n也想知道今天的你，被什么小事治愈了呢？"
  },
  {
    id: "product-launch",
    title: "新品发布 · Launch Story",
    mood: "适合品牌上新、手作周边、线上课程推广",
    summary: "聚焦亮点、体验、福利三段式，让用户快速理解价值。",
    styleInstruction:
      "保持专业但不僵硬，首段突出设计灵感或核心卖点，次段描述使用体验或使用场景，末段给出限时福利或行动号召，搭配品牌标签。",
    structure: ["亮点提炼：灵感 / 数值指标", "体验感受：真实情境、可视化语言", "行动号召：福利、时效、话题标签"],
    sample:
      "这次的新色灵感来自日落后的海雾，把温柔藏进丝雾质感里。\n上脸是轻盈的雾面光，通勤妆五分钟就能搞定，持妆 8 小时无折痕。\n限时 48 小时早鸟 9 折，评论区告诉我你最想尝试的色号吧～ #新品上架 #秋冬底妆"
  },
  {
    id: "travel-story",
    title: "旅行故事 · Wander Note",
    mood: "适合旅拍、城市漫游、周末野餐",
    summary: "像讲故事一样写旅程，让阅读者想继续往下走。",
    styleInstruction:
      "真实、细腻、有起伏。开头点出地点或地标，中段写一个小意外或对话，结尾引用一句旅行感悟并抛出下一站互动问题。",
    structure: ["地点点题：一句话打动读者的画面感", "故事核心：遇见的人/事/插曲", "情绪落点：一句金句+互动邀请"],
    sample:
      "在清迈古城的巷子里，偶遇一棵盛放的鸡蛋花，味道和阳光一起落在肩头。\n旅途永远会有意外：错过的巴士换成街角咖啡，才发现闲下来更能读懂城市。\n#旅行碎片 #清迈 下一站，你想去哪？一起换灵感吧。"
  },
  {
    id: "fitness-energy",
    title: "能量打卡 · Momentum",
    mood: "适合健身房、跑步、舞蹈训练等高动感内容",
    summary: "强节奏、可量化，给自己和读者都注入一点能量。",
    styleInstruction:
      "使用动词+感受的节奏，写出训练项目与完成数据，记录一个想放弃却坚持下来的瞬间，末尾设定下一个小目标，适度使用力量感 emoji。",
    structure: ["数据复盘：日序+项目+进度", "情绪突破：真实的内心独白或对话", "目标设定：下一个挑战 + 自我鼓励"],
    sample:
      "Day 42，5km 晨跑，平均配速 5'20\"，心跳和晚风一起越跑越坦荡。\n第三公里差点想放弃，还好耳机里的鼓点把我往前推了一下。\n目标是下周坚持三次训练，等着看进化成果 💪 #晨跑打卡"
  }
];

export default function TemplatesPage() {
  const router = useRouter();
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const copyInstruction = async (instruction: string, id: string) => {
    try {
      await navigator.clipboard.writeText(instruction);
      setCopiedId(id);
      window.setTimeout(() => setCopiedId((current) => (current === id ? null : current)), 2000);
    } catch {
      setCopiedId(null);
    }
  };

  const applyAndGo = (instruction: string) => {
    emitTemplateSelection(instruction);
    router.push("/upload");
  };

  return (
    <div style={{ display: "grid", gap: 32 }}>
      <Section
        title="灵感模板集"
        description="别急着从零开始，用精心拆解过的语气结构帮你快速进入创作状态。任选模板后，可直接跳转生成页继续调整。"
      >
        <div style={{ display: "grid", gap: 20, gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))" }}>
          {templates.map((tpl) => (
            <Card key={tpl.id} style={{ padding: 28, display: "grid", gap: 16, borderRadius: radii.xl }}>
              <div style={{ display: "grid", gap: 8 }}>
                <div style={{ display: "flex", gap: 8, alignItems: "center", flexWrap: "wrap" }}>
                  <h2 style={{ margin: 0, fontSize: 20 }}>{tpl.title}</h2>
                  <Badge tone="neutral">{tpl.mood}</Badge>
                </div>
                <p style={{ margin: 0, color: palette.textMuted, lineHeight: 1.6 }}>{tpl.summary}</p>
              </div>

              <div style={{ display: "grid", gap: 8 }}>
                <strong style={{ color: palette.text }}>写作提示</strong>
                <p style={{ margin: 0, lineHeight: 1.6 }}>{tpl.styleInstruction}</p>
              </div>

              <div style={{ display: "grid", gap: 8 }}>
                <strong style={{ color: palette.text }}>结构拆解</strong>
                <ul style={{ margin: 0, paddingLeft: 18, color: palette.textMuted, lineHeight: 1.6 }}>
                  {tpl.structure.map((item) => (
                    <li key={item}>{item}</li>
                  ))}
                </ul>
              </div>

              <div style={{ display: "grid", gap: 8 }}>
                <strong style={{ color: palette.text }}>示例输出</strong>
                <Card
                  style={{
                    background: palette.surfaceMuted,
                    border: `1px solid ${palette.border}`,
                    padding: 16,
                    borderRadius: radii.lg
                  }}
                >
                  <pre style={{ margin: 0, whiteSpace: "pre-wrap", lineHeight: 1.6 }}>{tpl.sample}</pre>
                </Card>
              </div>

              <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
                <Button variant="ghost" onClick={() => copyInstruction(tpl.styleInstruction, tpl.id)}>
                  {copiedId === tpl.id ? "已复制提示" : "复制提示"}
                </Button>
                <Button onClick={() => applyAndGo(tpl.styleInstruction)}>带入生成流程</Button>
              </div>
            </Card>
          ))}
        </div>
      </Section>
    </div>
  );
}
