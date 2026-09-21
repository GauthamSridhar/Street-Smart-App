// Local, bounded latency baseline. Node 22+. Credentials are never printed.
import { readFileSync } from 'node:fs';
import { performance } from 'node:perf_hooks';
const base = process.env.PERF_BASE_URL || 'http://localhost:18088';
if (!['localhost', '127.0.0.1'].includes(new URL(base).hostname)) throw new Error('Local targets only');
const settings = Object.fromEntries(readFileSync(new URL('../.env', import.meta.url), 'utf8')
  .split(/\r?\n/).map(line => line.match(/^([A-Z_]+)=(.*)$/)).filter(Boolean).map(m => [m[1], m[2]]));
const samples = [];
let token;
async function call(path, options = {}) {
  const response = await fetch(base + path, { ...options, signal: AbortSignal.timeout(15000),
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers } });
  if (!response.ok) throw new Error(`HTTP ${response.status} on ${path.split('?')[0]}`);
  return response;
}
try {
  const start = performance.now();
  const login = await (await call('/api/users/login', { method: 'POST', body: JSON.stringify({ identifier: settings.ADMIN_EMAIL, password: settings.ADMIN_PASSWORD }) })).json();
  token = login.jwt;
  const loginMs = performance.now() - start;
  await call('/api/products/search?q=&page=0&size=20');
  let failures = 0, next = 0, totalElements = 0;
  const begun = performance.now();
  await Promise.all(Array.from({ length: 4 }, async () => {
    while (next++ < 100) {
      const started = performance.now();
      try { const page = await (await call('/api/products/search?q=&page=0&size=20')).json(); totalElements = page.totalElements; }
      catch { failures++; }
      samples.push(performance.now() - started);
    }
  }));
  samples.sort((a,b) => a-b);
  console.log(JSON.stringify({ timestamp: new Date().toISOString(), base, node: process.version,
    workload: 'authenticated catalogue search, 100 requests, 4 workers, one warmup', totalElements,
    requests: samples.length, failures, loginMs: +loginMs.toFixed(2),
    p50Ms: +samples[49].toFixed(2), p95Ms: +samples[94].toFixed(2),
    requestsPerSecond: +(samples.length / ((performance.now()-begun)/1000)).toFixed(2) }, null, 2));
  if (failures) process.exitCode = 1;
} finally {
  if (token) await call('/api/users/logout', { method: 'POST' });
}
