"use client";
import { useEffect, useState } from 'react';
import { apiFetch, apiBase, getClientId } from '../../lib/clientId';

export default function UploadPage() {
  const [imageUrl, setImageUrl] = useState('');
  const [insights, setInsights] = useState<any>(null);
  const [copies, setCopies] = useState<any>(null);
  const [selectedCopy, setSelectedCopy] = useState<string>('');
  const [rendering, setRendering] = useState<boolean>(false);
  const [renderResult, setRenderResult] = useState<any>(null);
  const [insightSummary, setInsightSummary] = useState<string>('');
  const [quota, setQuota] = useState<any>(null);

  const base = apiBase();

  useEffect(() => {
    getClientId();
    refreshQuota();
  }, []);

  const refreshQuota = async () => {
    const resp = await apiFetch('/quota');
    if (resp.ok) setQuota(await resp.json());
  };

  const doCaption = async () => {
    const resp = await apiFetch('/generate/caption', { method: 'POST', body: JSON.stringify({ imageUrl, ocr: false }) });
    const data = await resp.json();
    setInsights(data);
    setInsightSummary(data.summary || '');
  };

  const doCopy = async () => {
    const payload = { imageTags: insights?.labels || [], ocrText: insights?.ocrText || '', imageUrl, audienceTags: [], hotTopicsOn: true, stylePreset: 'lively' };
    const resp = await apiFetch('/generate/copy', { method: 'POST', body: JSON.stringify(payload) });
    if (resp.status === 402) { alert('额度不足，请明日再试或开通订阅'); return; }
    setCopies(await resp.json());
    refreshQuota();
  };

  const favoriteCopy = async () => {
    if (!selectedCopy) { alert('请先选择一条文案'); return; }
    const resp = await apiFetch('/history', { method: 'POST', body: JSON.stringify({ copyText: selectedCopy, imageUrl }) });
    if (resp.ok) {
      alert('已收藏选中文案');
    }
  };

  const doRender = async () => {
    if (!selectedCopy) return;
    setRendering(true);
    try {
      const payload = {
        templateId: 'basic-01',
        primaryColor: '#111111',
        fontFamily: 'system-ui',
        fontSize: 36,
        lineHeight: 1.4,
        margin: 24,
        watermarkOn: false,
        stickerSet: [],
        ratio: '1:1',
        resolution: '1080x1080',
        grid: { enabled: false, size: '3x3' },
        imageUrl,
        copyText: selectedCopy
      };
      const resp = await apiFetch('/image/render', { method: 'POST', body: JSON.stringify(payload) });
      if (resp.status === 402) { alert('额度不足，请明日再试或开通订阅'); return; }
      setRenderResult(await resp.json());
      refreshQuota();
    } finally {
      setRendering(false);
    }
  };

  return (
    <div>
      <h2>上传/解析</h2>
      {quota && (
        <div style={{ marginBottom: 8, padding: 8, background: '#fafafa', border: '1px solid #eee' }}>
          <div>游客今日剩余额度：{quota.visitorRemainingToday}</div>
          <div>订阅剩余总额度：{quota.remainingCredits} {quota.subscribed ? `(到期 ${quota.expiresAt ? new Date(quota.expiresAt).toLocaleDateString() : ''})` : ''}</div>
        </div>
      )}
      <input placeholder="图片URL" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} style={{ width: '60%' }} />
      <button onClick={doCaption} style={{ marginLeft: 8 }}>解析图片</button>

      {insights && (
        <div style={{ marginTop: 16 }}>
          <div>图片内容：{insightSummary}</div>
          <div>标签: {insights.labels?.join(', ')}</div>
          <button onClick={doCopy} style={{ marginTop: 8 }}>生成文案</button>
        </div>
      )}

      {copies && (
        <div style={{ marginTop: 16 }}>
          <h3>候选文案</h3>
          <ul>
            {copies.items?.map((i: any) => (
              <li key={i.id} style={{ marginBottom: 8 }}>
                <label style={{ cursor: 'pointer' }}>
                  <input type="radio" name="sel" onChange={() => setSelectedCopy(i.text)} />
                  <span style={{ marginLeft: 8 }}>{i.text}</span>
                </label>
              </li>
            ))}
          </ul>
          <div style={{ marginTop: 8 }}>
            <button disabled={!selectedCopy || rendering} onClick={doRender}>渲染成图</button>
            <button style={{ marginLeft: 8 }} disabled={!selectedCopy} onClick={() => navigator.clipboard.writeText(selectedCopy)}>复制文案</button>
            <button style={{ marginLeft: 8 }} disabled={!selectedCopy} onClick={favoriteCopy}>收藏文案</button>
          </div>
        </div>
      )}

      {renderResult?.images && (
        <div style={{ marginTop: 16 }}>
          <h3>渲染结果</h3>
          {renderResult.images.map((url: string, idx: number) => (
            <div key={idx} style={{ marginBottom: 12 }}>
              <img src={url} alt={`render-${idx}`} style={{ maxWidth: 300, border: '1px solid #eee' }} />
              <div>
                <a href={url} target="_blank">打开原图</a>
              </div>
            </div>
          ))}
          <div>
            <button onClick={async () => {
              if (!renderResult?.generationId) return;
              await apiFetch('/favorites', { method: 'POST', body: JSON.stringify({ generationId: renderResult.generationId, favorite: true }) });
              alert('已收藏');
            }}>收藏</button>
          </div>
        </div>
      )}
    </div>
  );
}
