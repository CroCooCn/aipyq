"use client";
import { useEffect, useState } from 'react';
import { apiFetch, apiBase, getClientId } from '../../lib/clientId';

export default function UploadPage() {
  const [imageUrl, setImageUrl] = useState('');
  const [copyItems, setCopyItems] = useState<any[]>([]);
  const [selectedCopy, setSelectedCopy] = useState<string>('');
  const [rendering, setRendering] = useState<boolean>(false);
  const [renderResult, setRenderResult] = useState<any>(null);
  const [quota, setQuota] = useState<any>(null);
  const [instruction, setInstruction] = useState<string>('');
  const [reasoningOn, setReasoningOn] = useState<boolean>(false);

  const base = apiBase();

  useEffect(() => {
    getClientId();
    refreshQuota();
  }, []);

  const refreshQuota = async () => {
    const resp = await apiFetch('/quota');
    if (resp.ok) setQuota(await resp.json());
  };

  const doCopy = async () => {
    const payload = { imageUrl, audienceTags: [], hotTopicsOn: true, stylePreset: 'lively', instruction, reasoningOn };
    const resp = await apiFetch('/generate/copy', { method: 'POST', body: JSON.stringify(payload) });
    if (resp.status === 402) { alert('额度不足，请明日再试或开通订阅'); return; }
    const data = await resp.json();
    const items = Array.isArray(data?.items) ? data.items : [];
    setCopyItems(prev => [...prev, ...items]);
    refreshQuota();
  };

  const saveToHistory = async () => {
    if (!selectedCopy) { alert('请先选择一条文案'); return; }
    const resp = await apiFetch('/history', { method: 'POST', body: JSON.stringify({ copyText: selectedCopy, imageUrl }) });
    if (resp.ok) {
      alert('已保存到历史');
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
      <h2>生成文案</h2>
      {quota && (
        <div style={{ marginBottom: 8, padding: 8, background: '#fafafa', border: '1px solid #eee' }}>
          <div>游客今日剩余额度：{quota.visitorRemainingToday}</div>
          <div>订阅剩余总额度：{quota.remainingCredits} {quota.subscribed ? `(到期 ${quota.expiresAt ? new Date(quota.expiresAt).toLocaleDateString() : ''})` : ''}</div>
        </div>
      )}
      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
        <div style={{ flex: 1 }}>
          <div>
            <input placeholder="图片URL" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} style={{ width: '60%' }} />
            <label style={{ marginLeft: 12, cursor: 'pointer' }}>
              <input type="checkbox" checked={reasoningOn} onChange={(e) => setReasoningOn(e.target.checked)} /> 使用深度思考
            </label>
            <button onClick={doCopy} style={{ marginLeft: 8 }}>生成文案</button>
          </div>

          <div style={{ marginTop: 12 }}>
            <input placeholder="修改指令（例如：更文雅一点）" value={instruction} onChange={(e) => setInstruction(e.target.value)} style={{ width: '60%' }} />
            <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>将作为偏好影响下一次生成，并将结果追加在列表末尾</div>
          </div>

          <div style={{ marginTop: 16 }}>
            <h3>候选文案</h3>
            <ul>
              {copyItems.map((i: any, idx: number) => (
                <li key={(i.id || idx)} style={{ marginBottom: 8 }}>
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
              <button style={{ marginLeft: 8 }} disabled={!selectedCopy} onClick={saveToHistory}>保存到历史</button>
            </div>
          </div>
        </div>
        <div style={{ width: 360 }}>
          <h3>渲染结果</h3>
          {renderResult?.images ? (
            <div>
              {renderResult.images.map((url: string, idx: number) => (
                <div key={idx} style={{ marginBottom: 12 }}>
                  <img src={url} alt={`render-${idx}`} style={{ maxWidth: 340, border: '1px solid #eee' }} />
                  <div>
                    <a href={url} target="_blank">打开原图</a>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div style={{ color: '#888' }}>暂无渲染内容</div>
          )}
        </div>
      </div>
    </div>
  );
}
