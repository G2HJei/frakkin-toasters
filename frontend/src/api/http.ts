// REST calls to the backend. The session cookie goes along, and a call that changes something sends the CSRF token from
// the XSRF-TOKEN cookie in the X-XSRF-TOKEN header, which Spring Security checks. A tab with a dev user of its own
// names him in X-Dev-User (devTab.ts).

import { tabUser } from './devTab';

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
  }
}

export function readCookie(name: string): string | null {
  const match = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${name}=`));
  return match ? decodeURIComponent(match.substring(name.length + 1)) : null;
}

// The server hands out the token on any request, so a fresh browser asks once before its first change.
async function csrfToken(): Promise<string> {
  let token = readCookie('XSRF-TOKEN');
  if (!token) {
    await fetch('/api/login/providers', { credentials: 'same-origin' });
    token = readCookie('XSRF-TOKEN');
  }
  return token ?? '';
}

export async function api<T>(method: 'GET' | 'POST' | 'PUT' | 'DELETE', path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  const devUser = tabUser();
  if (devUser) {
    headers['X-Dev-User'] = devUser;
  }
  if (method !== 'GET') {
    headers['X-XSRF-TOKEN'] = await csrfToken();
  }
  let payload: BodyInit | undefined;
  if (typeof body === 'string') {
    headers['Content-Type'] = 'application/json';
    payload = body;
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }
  const response = await fetch(path, { method, headers, body: payload, credentials: 'same-origin' });
  const text = await response.text();
  if (!response.ok) {
    throw new ApiError(response.status, errorMessage(text) ?? `${response.status} ${response.statusText}`);
  }
  return (text ? JSON.parse(text) : null) as T;
}

function errorMessage(text: string): string | null {
  try {
    const json = JSON.parse(text) as { message?: string; detail?: string; error?: string };
    return json.message || json.detail || json.error || null;
  } catch {
    return text || null;
  }
}
