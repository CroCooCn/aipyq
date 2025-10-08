"use client";
import { useState } from 'react';

export default function UploadPage() {
  const [imageUrl, setImageUrl] = useState('');
  const [insights, setInsights] = useState<any>(null);
  const [copies, setCopies] = useState<any>(null);
  const [selectedCopy, setSelectedCopy] = useState<string>('');
  const [rendering, setRendering] = useState<boolean>(false);
  const [renderResult, setRenderResult] = useState<any>(null);

  const apiBase =  'http://localhost:8080/api/v1';

  const doCaption = async () => {
    const resp = await fetch(`${apiBase}/generate/caption`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ imageUrl, ocr: true }) });
    setInsights(await resp.json());
  };

  const doCopy = async () => {
    const payload = { imageTags: insights?.labels || [], ocrText: insights?.ocrText || '', imageUrl, audienceTags: [], hotTopicsOn: true, stylePreset: 'lively' };
    const resp = await fetch(`${apiBase}/generate/copy`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    setCopies(await resp.json());
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
      const resp = await fetch(`${apiBase}/image/render`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
      setRenderResult(await resp.json());
    } finally {
      setRendering(false);
    }
  };

  return (
    <div>
      <h2>上传/解析</h2>
      <input placeholder="图片URL" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} style={{ width: '60%' }} />
      <button onClick={doCaption} style={{ marginLeft: 8 }}>解析图片</button>

      {insights && (
        <div style={{ marginTop: 16 }}>
          <div>标签: {insights.labels?.join(', ')}</div>
          <div>OCR: {insights.ocrText}</div>
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
        </div>
      )}
    </div>
  );
}
