import * as auth from './auth';

// Java resource server by default; use 4102 for the Python server.
const API_BASE = 'http://localhost:4101';

export async function me(): Promise<Record<string, unknown>> {
  const token = auth.getAccessToken();
  if (!token) {
    throw new Error('Not signed in');
  }
  const res = await fetch(`${API_BASE}/api/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) {
    throw new Error(`GET /api/me failed (${res.status})`);
  }
  return res.json();
}
