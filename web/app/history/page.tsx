"use client";
import { useEffect, useState } from 'react';
import { apiFetch } from '../../lib/clientId';

export default function HistoryPage() {
  const [items, setItems] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [onlyFav, setOnlyFav] = useState(false);

  const load = async () => {
    const path = onlyFav ? '/favorites' : '/history';
    const resp = await apiFetch(path);
    if (!resp.ok) return;
    const data = await resp.json();
    setItems(data.items || []);
    setTotal(data.total || 0);
  };

  useEffect(() => { load(); }, [onlyFav]);

  const toggleFav = async (id: string, favorite: boolean) => {
    await apiFetch('/favorites', { method: 'POST', body: JSON.stringify({ generationId: id, favorite }) });
    load();
  };

  const del = async (id: string) => {
    await apiFetch(`/history/${id}`, { method: 'DELETE' });
    load();
  };

  return (
    <div>
      <h2>历史与收藏</h2>
      <div style={{ marginTop: 8 }}>
        <label style={{ cursor: 'pointer' }}>
          <input type="checkbox" checked={onlyFav} onChange={(e) => setOnlyFav(e.target.checked)} /> 只看收藏
        </label>
      </div>
      <div>总数：{total}</div>
      <ul style={{ marginTop: 12 }}>
        {items.map((it: any) => (
          <li key={it.id} style={{ padding: 8, borderBottom: '1px solid #eee' }}>
            <div>时间：{it.createdAt ? new Date(it.createdAt).toLocaleString() : ''}</div>
            <div>文案：{it.selectedCopy}</div>
            <div style={{ marginTop: 6 }}>
              <button onClick={() => toggleFav(it.id, !(it.favorite))}>{it.favorite ? '取消收藏' : '收藏'}</button>
              <button style={{ marginLeft: 8 }} onClick={() => del(it.id)}>删除</button>
              <button style={{ marginLeft: 8 }} onClick={() => navigator.clipboard.writeText(it.selectedCopy)}>复制文案</button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
