"use client";

import { useCallback, useEffect, useMemo, useState, ChangeEvent } from "react";
import { apiFetch } from "../../lib/api";
import { getClientId } from "../../lib/clientId";
import { TEMPLATE_EVENT, TEMPLATE_STORAGE_KEY } from "../../lib/templates";
import { Badge, Button, Card, Divider, InfoRow, Section, Stepper, palette, radii } from "../../components/ui";

type Mode = "single" | "batch";

type CopyItem = {
  id: string;
  text: string;
};

type RenderResult = {
  images?: string[];
  generationId?: string;
};

type BatchRenderInfo = {
  status: "pending" | "working" | "done" | "error";
  result?: RenderResult;
  error?: string;
};

const tonePresets = [
  {
    key: "warm",
    label: "温柔日常",
    description: "轻松、治愈、有画面感",
    instruction:
      "语气温暖、细节入手、轻柔内心独白，适度加入 emoji（不超过 2 个），结尾以提问或祝福轻轻收束。"
  },
  {
    key: "launch",
    label: "新品发布",
    description: "结构清晰、亮点突出",
    instruction:
      "分三段说明设计灵感、核心卖点、行动号召，用专业但不冰冷的语气，适当使用品牌标签和数字化描述。"
  },
  {
    key: "story",
    label: "旅行故事",
    description: "情绪起伏、沉浸感强",
    instruction:
      "开头点出地点或画面细节，中段分享一个小插曲或意外收获，结尾抛出思考或邀请互动，保持真实口吻。"
  },
  {
    key: "punch",
    label: "燃力打卡",
    description: "节奏紧凑、鼓舞人心",
    instruction:
      "使用动词+感受的短句节奏，写出训练内容、突破瞬间、给自己的鼓励，整体干净有冲劲，适度加入数据。"
  }
];

export default function UploadPage() {
  const [mode, setMode] = useState<Mode>("single");
  const [styleInstruction, setStyleInstruction] = useState("");
  const [selectedTone, setSelectedTone] = useState<string | null>(null);
  const [reasoningOn, setReasoningOn] = useState(false);

  const [imageUrl, setImageUrl] = useState("");
  const [batchImages, setBatchImages] = useState<string[]>([]);
  const [batchUrlInput, setBatchUrlInput] = useState("");
  const [batchStatus, setBatchStatus] = useState<Record<string, BatchRenderInfo>>({});

  const [copies, setCopies] = useState<CopyItem[]>([]);
  const [selectedCopy, setSelectedCopy] = useState("");
  const [renderResult, setRenderResult] = useState<RenderResult | null>(null);

  const [generating, setGenerating] = useState(false);
  const [rendering, setRendering] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getClientId();
  }, []);

  const resetFeedback = useCallback(() => {
    setMessage(null);
    setError(null);
  }, []);

  const uploadToOss = useCallback(async (file: File) => {
    const form = new FormData();
    form.append("file", file);
    const resp = await apiFetch("/upload/image", { method: "POST", body: form });
    const data = await resp.json().catch(() => ({}));
    if (!resp.ok || !data?.url) {
      throw new Error((data as any)?.message || "上传失败，请稍后再试");
    }
    return data.url as string;
  }, []);

  const addBatchImage = useCallback((url: string) => {
    setBatchImages((prev) => {
      if (prev.includes(url)) return prev;
      return [...prev, url];
    });
  }, []);

  const removeBatchImage = (url: string) => {
    setBatchImages((prev) => prev.filter((item) => item !== url));
    setBatchStatus((prev) => {
      const next = { ...prev };
      delete next[url];
      return next;
    });
  };

  const toneByKey = useMemo(() => Object.fromEntries(tonePresets.map((tone) => [tone.key, tone])), []);

  const applyTone = (key: string) => {
    const tone = toneByKey[key];
    if (!tone) return;
    setSelectedTone(key);
    setStyleInstruction(tone.instruction);
    setMessage(`已套用「${tone.label}」风格模板，可在下方微调。`);
  };

  useEffect(() => {
    const applyTemplate = () => {
      if (typeof window === "undefined") return;
      const saved = window.localStorage.getItem(TEMPLATE_STORAGE_KEY);
      if (saved) {
        setStyleInstruction(saved);
        setSelectedTone(null);
        setMessage("已从模板页带入风格指令。");
        window.localStorage.removeItem(TEMPLATE_STORAGE_KEY);
      }
    };
    applyTemplate();
    window.addEventListener(TEMPLATE_EVENT, applyTemplate);
    const storageHandler = (event: StorageEvent) => {
      if (event.key === TEMPLATE_STORAGE_KEY) {
        applyTemplate();
      }
    };
    window.addEventListener("storage", storageHandler);
    return () => {
      window.removeEventListener(TEMPLATE_EVENT, applyTemplate);
      window.removeEventListener("storage", storageHandler);
    };
  }, []);

  const handleFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
    resetFeedback();
    const files = event.target.files;
    if (!files || files.length === 0) return;
    try {
      if (mode === "single") {
        const url = await uploadToOss(files[0]);
        setImageUrl(url);
        setMessage("图片上传成功，已自动填入链接。");
      } else {
        const uploaded: string[] = [];
        for (const file of Array.from(files)) {
          const url = await uploadToOss(file);
          addBatchImage(url);
          uploaded.push(url);
        }
        if (uploaded.length > 0) {
          setMessage(`成功添加 ${uploaded.length} 张图片。`);
        }
      }
    } catch (err: any) {
      setError(err?.message || "上传失败，请稍后再试");
    } finally {
      event.target.value = "";
    }
  };

  useEffect(() => {
    const onPaste = async (event: ClipboardEvent) => {
      const items = event.clipboardData?.items;
      if (!items) return;
      for (const item of items) {
        if (item.type.startsWith("image/")) {
          const file = item.getAsFile();
          if (!file) continue;
          try {
            const url = await uploadToOss(file);
            if (mode === "single") {
              setImageUrl(url);
            } else {
              addBatchImage(url);
            }
            setMessage("粘贴图片成功，已上传至素材库。");
          } catch (err: any) {
            setError(err?.message || "粘贴图片失败，请重试");
          }
          break;
        }
      }
    };
    window.addEventListener("paste", onPaste);
    return () => window.removeEventListener("paste", onPaste);
  }, [mode, uploadToOss, addBatchImage, resetFeedback]);

  const clearAll = () => {
    resetFeedback();
    setCopies([]);
    setSelectedCopy("");
    setRenderResult(null);
    setBatchStatus({});
    if (mode === "single") {
      setImageUrl("");
    } else {
      setBatchImages([]);
    }
  };

  useEffect(() => {
    clearAll();
  }, [mode]);

  const handleGenerate = async () => {
    resetFeedback();
    if (!styleInstruction.trim()) {
      setError("请先设置文案的风格指令，可选择预设模板或自定义。");
      return;
    }
    if (mode === "single") {
      if (!imageUrl.trim()) {
        setError("请上传或填写一张图片链接。");
        return;
      }
    } else if (batchImages.length === 0) {
      setError("请至少添加一张图片。");
      return;
    }

    setGenerating(true);
    try {
      const body =
        mode === "single"
          ? { imageUrl: imageUrl.trim(), styleInstruction: styleInstruction.trim(), reasoningOn }
          : { imageUrls: batchImages, styleInstruction: styleInstruction.trim(), reasoningOn };
      const endpoint = mode === "single" ? "/generate/copy" : "/generate/copy/batch";
      const resp = await apiFetch(endpoint, { method: "POST", body: JSON.stringify(body) });
      if (resp.status === 401) {
        setError("请先登录后再生成文案。");
        return;
      }
      if (resp.status === 402) {
        setError("点券不足，请先充值或调整批量数量。");
        return;
      }
      if (!resp.ok) {
        const text = await resp.text();
        throw new Error(text || "文案生成失败，请稍后再试");
      }
      const data = await resp.json();
      const items: CopyItem[] = Array.isArray(data?.items) ? data.items : [];
      setCopies(items);
      setSelectedCopy(items[0]?.text || "");
      setRenderResult(null);
      setBatchStatus({});
      setMessage(items.length > 0 ? "文案生成完成，请挑选意向版本。" : "暂未生成文案，请尝试调整指令。");
    } catch (err: any) {
      setError(err?.message || "文案生成失败，请稍后再试");
    } finally {
      setGenerating(false);
    }
  };

  const handleRender = async () => {
    resetFeedback();
    if (!selectedCopy) {
      setError("请先选择一条文案。");
      return;
    }
    if (mode === "single" && !imageUrl.trim()) {
      setError("请上传或填写图片链接。");
      return;
    }
    if (mode === "batch" && batchImages.length === 0) {
      setError("请至少添加一张图片。");
      return;
    }

    if (mode === "single") {
      setRendering(true);
      try {
        const resp = await apiFetch("/image/render", {
          method: "POST",
          body: JSON.stringify({
            templateId: "basic",
            ratio: "1:1",
            resolution: "1024x1024",
            prompt: selectedCopy,
            imageUrl: imageUrl.trim()
          })
        });
        if (resp.status === 401) {
          setError("请先登录后再生成成图。");
          return;
        }
        if (resp.status === 402) {
          setError("点券不足，无法生成成图。");
          return;
        }
        if (!resp.ok) {
          const text = await resp.text();
          throw new Error(text || "成图生成失败，请稍后再试");
        }
        const data = (await resp.json()) as RenderResult;
        setRenderResult(data);
        setMessage("成图生成完成，点击图片可查看原图。");
      } catch (err: any) {
        setError(err?.message || "成图生成失败，请稍后再试");
      } finally {
        setRendering(false);
      }
      return;
    }

    setRendering(true);
    const initial: Record<string, BatchRenderInfo> = {};
    batchImages.forEach((url) => (initial[url] = { status: "pending" }));
    setBatchStatus(initial);

    try {
      for (const url of batchImages) {
        setBatchStatus((prev) => ({ ...prev, [url]: { status: "working" } }));
        const resp = await apiFetch("/image/render", {
          method: "POST",
          body: JSON.stringify({
            templateId: "basic",
            ratio: "1:1",
            resolution: "1024x1024",
            prompt: selectedCopy,
            imageUrl: url
          })
        });
        if (resp.status === 401) {
          setError("登录状态已失效，请重新登录。");
          setBatchStatus((prev) => ({ ...prev, [url]: { status: "error", error: "未登录" } }));
          break;
        }
        if (resp.status === 402) {
          setError("点券不足，成图任务提前终止。");
          setBatchStatus((prev) => ({ ...prev, [url]: { status: "error", error: "点券不足" } }));
          break;
        }
        if (!resp.ok) {
          const text = await resp.text();
          throw new Error(text || "成图生成失败，请稍后再试");
        }
        const data = (await resp.json()) as RenderResult;
        setBatchStatus((prev) => ({ ...prev, [url]: { status: "done", result: data } }));
      }
      setMessage("批量成图任务完成。");
    } catch (err: any) {
      const info = err?.message || "成图生成失败，请稍后再试";
      setError(info);
      setBatchStatus((prev) => {
        const next = { ...prev };
        for (const url of batchImages) {
          if (next[url]?.status === "working" || next[url]?.status === "pending") {
            next[url] = { status: "error", error: info };
          }
        }
        return next;
      });
    } finally {
      setRendering(false);
    }
  };

  const currentStep = useMemo(() => {
    if (copies.length > 0) return 2;
    if (mode === "single") {
      return imageUrl ? 1 : 0;
    }
    return batchImages.length > 0 ? 1 : 0;
  }, [copies.length, mode, imageUrl, batchImages.length]);

  return (
    <div style={{ display: "grid", gap: 36 }}>
      <Section
        title="生成中心"
        description="遵循三步流程：定义语气 → 准备素材 → 择优发布。每一步都可以回滚，从历史档案中复用。"
        action={
          <Badge tone="success">
            {mode === "single" ? "单图模式 · 适合快速发布" : `批量模式 · 已选择 ${batchImages.length} 张素材`}
          </Badge>
        }
      >
        <Card style={{ padding: 24, display: "grid", gap: 24 }}>
          <Stepper
            current={currentStep}
            steps={[
              { title: "设定语气", description: "选择模板或输入你想要的文案风格" },
              { title: "整理素材", description: "上传图片或粘贴链接，支持多图批量" },
              { title: "输出交付", description: "挑选文案、生成成图、保存历史" }
            ]}
          />
          <Divider />
          <div style={{ display: "grid", gap: 24 }}>
            <Card style={{ padding: 24, display: "grid", gap: 20 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div style={{ display: "grid", gap: 8 }}>
                  <h3 style={{ margin: 0 }}>Step 1 · 语气模板</h3>
                  <p style={{ margin: 0, color: palette.textMuted, lineHeight: 1.6 }}>
                    选择一个适配的语气基调，可在此基础上调整措辞和表达重点。模板页中的灵感也会同步到这里。
                  </p>
                </div>
                <Button variant="ghost" style={{ borderColor: palette.border }} onClick={() => window.open("/templates", "_blank")}>
                  查看全部模板
                </Button>
              </div>
              <div style={{ display: "grid", gap: 12, gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))" }}>
                {tonePresets.map((tone) => {
                  const active = selectedTone === tone.key;
                  return (
                    <div
                      key={tone.key}
                      onClick={() => applyTone(tone.key)}
                      style={{
                        border: `1px solid ${active ? palette.primary : palette.border}`,
                        background: active ? palette.primarySoft : palette.surface,
                        borderRadius: radii.lg,
                        padding: 18,
                        display: "grid",
                        gap: 8,
                        cursor: "pointer",
                        transition: "all 150ms ease"
                      }}
                    >
                      <span style={{ fontWeight: 600 }}>{tone.label}</span>
                      <span style={{ fontSize: 12, color: palette.textMuted }}>{tone.description}</span>
                      <span style={{ fontSize: 12, color: palette.primary }}>点击套用</span>
                    </div>
                  );
                })}
              </div>
              <label style={{ display: "grid", gap: 8 }}>
                <span style={{ fontWeight: 600 }}>自定义风格描述</span>
                <textarea
                  value={styleInstruction}
                  onChange={(e) => setStyleInstruction(e.target.value)}
                  rows={4}
                  placeholder="例如：语气克制但有力量，开头抛出洞察，中间用三个排比句铺陈情绪，结尾留一个互动问题。"
                  style={{
                    borderRadius: radii.md,
                    border: `1px solid ${palette.border}`,
                    padding: 14,
                    resize: "vertical",
                    fontSize: 14,
                    lineHeight: 1.6
                  }}
                />
              </label>
              <label style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <input type="checkbox" checked={reasoningOn} onChange={(e) => setReasoningOn(e.target.checked)} />
                <span style={{ fontSize: 13, color: palette.textMuted }}>开启推理增强模式（生成前让 AI 先进行内部分析，更贴合复杂场景）。</span>
              </label>
            </Card>

            <Card style={{ padding: 24, display: "grid", gap: 20 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 16 }}>
                <div style={{ display: "grid", gap: 8 }}>
                  <h3 style={{ margin: 0 }}>Step 2 · 素材管理</h3>
                  <p style={{ margin: 0, color: palette.textMuted, lineHeight: 1.6 }}>
                    支持本地上传、粘贴或输入链接。批量模式下可拖拽排序，生成会按列表顺序依次处理。
                  </p>
                </div>
                <div
                  style={{
                    background: palette.surfaceMuted,
                    borderRadius: radii.pill,
                    display: "inline-flex",
                    padding: 6,
                    gap: 6
                  }}
                >
                  <button
                    type="button"
                    onClick={() => setMode("single")}
                    style={{
                      padding: "8px 16px",
                      borderRadius: radii.pill,
                      border: "none",
                      background: mode === "single" ? palette.primary : "transparent",
                      color: mode === "single" ? "#fff" : palette.textMuted,
                      fontWeight: 600,
                      cursor: "pointer"
                    }}
                  >
                    单图模式
                  </button>
                  <button
                    type="button"
                    onClick={() => setMode("batch")}
                    style={{
                      padding: "8px 16px",
                      borderRadius: radii.pill,
                      border: "none",
                      background: mode === "batch" ? palette.primary : "transparent",
                      color: mode === "batch" ? "#fff" : palette.textMuted,
                      fontWeight: 600,
                      cursor: "pointer"
                    }}
                  >
                    批量模式
                  </button>
                </div>
              </div>

              <label
                style={{
                  border: `1px dashed ${palette.borderStrong}`,
                  borderRadius: radii.xl,
                  padding: 28,
                  display: "grid",
                  gap: 8,
                  placeItems: "center",
                  textAlign: "center",
                  background: palette.surfaceMuted
                }}
              >
                <strong>拖拽文件到此处，或点击选择本地图片</strong>
                <span style={{ fontSize: 13, color: palette.textMuted }}>支持粘贴剪贴板图片，系统会自动上传到素材库。</span>
                <input type="file" accept="image/*" multiple={mode === "batch"} onChange={handleFileChange} style={{ display: "none" }} />
              </label>

              {mode === "single" ? (
                <div style={{ display: "grid", gap: 12 }}>
                  <label style={{ display: "grid", gap: 6 }}>
                    <span style={{ fontWeight: 600 }}>图片链接</span>
                    <input
                      value={imageUrl}
                      onChange={(e) => setImageUrl(e.target.value)}
                      placeholder="https://..."
                      style={{
                        padding: 12,
                        border: `1px solid ${palette.border}`,
                        borderRadius: radii.md
                      }}
                    />
                  </label>
                  {imageUrl ? (
                    <Card style={{ padding: 16, display: "flex", alignItems: "center", gap: 16 }}>
                      <img
                        src={imageUrl}
                        alt="preview"
                        style={{ width: 120, height: 120, objectFit: "cover", borderRadius: radii.md, border: `1px solid ${palette.border}` }}
                        onError={() => setMessage("预览加载失败，但链接仍可用于生成。")}
                      />
                      <InfoRow label="图片地址" value={<span style={{ wordBreak: "break-all", fontSize: 13 }}>{imageUrl}</span>} />
                    </Card>
                  ) : null}
                </div>
              ) : (
                <div style={{ display: "grid", gap: 16 }}>
                  <label style={{ display: "grid", gap: 6 }}>
                    <span style={{ fontWeight: 600 }}>粘贴图片链接</span>
                    <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
                      <input
                        value={batchUrlInput}
                        onChange={(e) => setBatchUrlInput(e.target.value)}
                        placeholder="粘贴一条网络图片链接后点击添加"
                        style={{
                          flex: "1 1 260px",
                          padding: 12,
                          border: `1px solid ${palette.border}`,
                          borderRadius: radii.md
                        }}
                      />
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={() => {
                          const url = batchUrlInput.trim();
                          if (!url) return;
                          addBatchImage(url);
                          setBatchUrlInput("");
                          setMessage("已添加新的图片链接。");
                        }}
                      >
                        添加
                      </Button>
                    </div>
                  </label>

                  <div style={{ display: "grid", gap: 12 }}>
                    {batchImages.length === 0 ? (
                      <div
                        style={{
                          padding: 24,
                          borderRadius: radii.lg,
                          border: `1px dashed ${palette.borderStrong}`,
                          color: palette.textMuted,
                          textAlign: "center"
                        }}
                      >
                        暂无素材，请先上传或粘贴链接。
                      </div>
                    ) : (
                      batchImages.map((url, index) => {
                        const info = batchStatus[url];
                        return (
                          <div
                            key={url}
                            style={{
                              border: `1px solid ${palette.border}`,
                              borderRadius: radii.lg,
                              padding: 16,
                              display: "grid",
                              gap: 10,
                              background: palette.surface
                            }}
                          >
                            <div style={{ display: "flex", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
                              <div style={{ display: "grid", gap: 4 }}>
                                <span style={{ fontSize: 12, color: palette.textMuted }}>图片 {index + 1}</span>
                                <a href={url} target="_blank" rel="noreferrer" style={{ color: palette.primary, wordBreak: "break-all" }}>
                                  {url}
                                </a>
                              </div>
                              <div style={{ display: "flex", gap: 8 }}>
                                <Badge tone={info ? (info.status === "done" ? "success" : info.status === "error" ? "warning" : "neutral") : "neutral"}>
                                  {info
                                    ? info.status === "done"
                                      ? "已完成成图"
                                      : info.status === "error"
                                        ? info.error || "失败"
                                        : info.status === "working"
                                          ? "生成中"
                                          : "待处理"
                                    : "待处理"}
                                </Badge>
                                <Button variant="ghost" onClick={() => removeBatchImage(url)} style={{ padding: "6px 14px" }}>
                                  移除
                                </Button>
                              </div>
                            </div>
                            {info?.result?.images && info.result.images.length > 0 ? (
                              <div style={{ display: "grid", gap: 12, gridTemplateColumns: "repeat(auto-fill, minmax(120px, 1fr))" }}>
                                {info.result.images.map((img, idx) => (
                                  <a key={img || idx} href={img} target="_blank" rel="noreferrer" style={{ borderRadius: radii.md, overflow: "hidden" }}>
                                    <img
                                      src={img}
                                      alt={`render-${index}-${idx}`}
                                      style={{ width: "100%", aspectRatio: "1/1", objectFit: "cover", display: "block" }}
                                    />
                                  </a>
                                ))}
                              </div>
                            ) : null}
                          </div>
                        );
                      })
                    )}
                  </div>
                </div>
              )}
            </Card>
          </div>

          <Divider />

          <div style={{ display: "flex", gap: 16, flexWrap: "wrap" }}>
            <Button onClick={handleGenerate} disabled={generating}>
              {generating ? "生成中…" : mode === "single" ? "生成文案" : "批量生成文案"}
            </Button>
            <Button variant="secondary" onClick={handleRender} disabled={!selectedCopy || rendering}>
              {rendering ? "成图进行中…" : mode === "single" ? "生成成图" : "批量生成成图"}
            </Button>
            <Button variant="ghost" onClick={clearAll}>
              清空结果
            </Button>
          </div>
        </Card>
      </Section>

      {message || error ? (
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
      ) : null}

      <Section
        title="Step 3 · 文案挑选与成图回看"
        description="每次生成的候选文案会在这里列出，可快速复制、标记喜欢或在成图完成后查看结果。"
      >
        <Card style={{ display: "grid", gap: 24, padding: 24 }}>
          {copies.length === 0 ? (
            <div
              style={{
                padding: 32,
                textAlign: "center",
                borderRadius: radii.lg,
                border: `1px dashed ${palette.border}`,
                color: palette.textMuted
              }}
            >
              尚未生成文案，请完成前序步骤后点击“生成文案”。
            </div>
          ) : (
            <div style={{ display: "grid", gap: 16 }}>
              {copies.map((item, idx) => {
                const active = selectedCopy === item.text;
                return (
                  <div
                    key={item.id}
                    style={{
                      border: `2px solid ${active ? palette.primary : palette.border}`,
                      borderRadius: radii.lg,
                      padding: 20,
                      display: "grid",
                      gap: 12,
                      background: active ? palette.primarySoft : palette.surface
                    }}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12 }}>
                      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                        <Badge tone={active ? "success" : "neutral"}>{String(idx + 1).padStart(2, "0")}</Badge>
                        <span style={{ fontWeight: 600 }}>{active ? "已选中" : "候选文案"}</span>
                      </div>
                      <div style={{ display: "flex", gap: 8 }}>
                        <Button
                          variant={active ? "secondary" : "primary"}
                          onClick={() => setSelectedCopy(item.text)}
                          style={{ padding: "6px 16px" }}
                        >
                          {active ? "当前使用" : "选择这条"}
                        </Button>
                        <Button
                          variant="ghost"
                          style={{ padding: "6px 16px" }}
                          onClick={() => navigator.clipboard.writeText(item.text).then(() => setMessage("已复制到剪贴板。"))}
                        >
                          复制
                        </Button>
                      </div>
                    </div>
                    <p style={{ margin: 0, lineHeight: 1.7 }}>{item.text}</p>
                  </div>
                );
              })}
            </div>
          )}

          {mode === "single" ? (
            renderResult?.images && renderResult.images.length > 0 ? (
              <div style={{ display: "grid", gap: 16 }}>
                <Divider />
                <h4 style={{ margin: 0 }}>最新成图</h4>
                <div style={{ display: "grid", gap: 16, gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))" }}>
                  {renderResult.images.map((url, index) => (
                    <a
                      key={url || index}
                      href={url}
                      target="_blank"
                      rel="noreferrer"
                      style={{ borderRadius: radii.lg, overflow: "hidden", border: `1px solid ${palette.border}` }}
                    >
                      <img src={url} alt={`render-${index}`} style={{ width: "100%", display: "block" }} />
                    </a>
                  ))}
                </div>
              </div>
            ) : null
          ) : null}
        </Card>
      </Section>
    </div>
  );
}
