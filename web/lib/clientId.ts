export function getClientId(): string {
  if (typeof window === 'undefined') return '';
  const KEY = 'aipyq_client_id';
  let id = localStorage.getItem(KEY);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(KEY, id);
  }
  return id;
}

export function apiBase(): string {
  return 'http://localhost:8080/api/v1';
}

export async function apiFetch(path: string, init: RequestInit = {}) {
  const base = apiBase();
  const headers = new Headers(init.headers);
  const cid = getClientId();
  if (cid) headers.set('X-Client-Id', cid);
  headers.set('Content-Type', headers.get('Content-Type') || 'application/json');
  return fetch(`${base}${path}`, { ...init, headers });
}

