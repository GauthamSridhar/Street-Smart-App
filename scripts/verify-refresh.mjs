// Uses only the dedicated acceptance administrator and isolated web port.
import assert from 'node:assert/strict';
const base = 'http://localhost:18088/api';
async function login() {
  const response = await fetch(base + '/users/login', { method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ identifier: 'acceptance-admin@example.test', password: 'Acceptance-only-2026!' }) });
  assert.equal(response.status, 200);
  const cookie = response.headers.get('set-cookie');
  assert.match(cookie, /HttpOnly/); assert.match(cookie, /SameSite=Strict/);
  assert.equal(response.headers.get('cache-control'), 'no-store');
  return { jwt: (await response.json()).jwt, cookie: cookie.split(';')[0] };
}
const refresh = cookie => fetch(base + '/users/refresh', { method: 'POST',
  headers: { Cookie: cookie, 'X-Requested-With': 'StreetSmart' } });
const protectedRequest = (path, jwt, method = 'GET') => fetch(base + path, {
  method, headers: { Authorization: 'Bearer ' + jwt },
});
let active;
try {
  active = await login();
  assert.equal((await fetch(base + '/users/refresh', { method: 'POST', headers: { Cookie: active.cookie } })).status, 403);
  const rotated = await refresh(active.cookie);
  assert.equal(rotated.status, 200);
  const replacement = rotated.headers.get('set-cookie').split(';')[0];
  assert.notEqual(replacement, active.cookie);
  const jwt = (await rotated.json()).jwt;
  assert.equal((await protectedRequest('/users/me', jwt)).status, 200);
  assert.equal((await refresh(active.cookie)).status, 401);
  assert.equal((await refresh(replacement)).status, 401);
  assert.equal((await protectedRequest('/users/me', jwt)).status, 401);
  active = await login();
  assert.equal((await protectedRequest('/users/logout', active.jwt, 'POST')).status, 204);
  assert.equal((await refresh(active.cookie)).status, 401);
  active = await login();
  const second = await login();
  assert.equal((await protectedRequest('/users/logout-all', active.jwt, 'POST')).status, 204);
  for (const session of [active, second]) assert.equal((await refresh(session.cookie)).status, 401);
  console.log('PASS: gateway cookies, rotation, replay revocation, protected API denial, logout and logout-all on PostgreSQL.');
} finally {
  if (active) await protectedRequest('/users/logout', active.jwt, 'POST');
}
