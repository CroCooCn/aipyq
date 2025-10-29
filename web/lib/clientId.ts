'use client';

const CLIENT_STORAGE_KEY = 'aipyq_client_id';

export function getClientId(): string {
  if (typeof window === 'undefined') {
    return '';
  }
  let id = window.localStorage.getItem(CLIENT_STORAGE_KEY);
  if (!id) {
    id = window.crypto.randomUUID();
    window.localStorage.setItem(CLIENT_STORAGE_KEY, id);
  }
  return id;
}
