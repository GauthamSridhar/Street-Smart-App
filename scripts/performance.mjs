// Local response-completion baseline. Node 22+. Credentials are never printed.
import { existsSync, readFileSync } from 'node:fs';
import { performance } from 'node:perf_hooks';
const base = process.env.PERF_BASE_URL || 'http://localhost:18088';
const target = new URL(base);
if (!['localhost', '127.0.0.1'].includes(target.hostname)) throw new Error('Local targets only');
const isolated = process.env.PERF_E2E === 'true';
if (isolated && (target.port !== '18088' || target.protocol !== 'http:')) throw new Error('E2E credentials are only allowed on the isolated local web port 18088');
const requests = positiveInteger('PERF_REQUESTS', 100, 10000);
const workers = positiveInteger('PERF_WORKERS', 4, 32);
const warmups = positiveInteger('PERF_WARMUPS', 10, 100);
const suite = process.env.PERF_SUITE === 'true';
const query = process.env.PERF_QUERY || '';
const allowEmptyCatalogue = process.env.PERF_ALLOW_EMPTY_CATALOGUE === 'true';
const envFile = new URL('../.env', import.meta.url);
const settings = isolated ? { ADMIN_EMAIL: 'acceptance-admin@example.test', ADMIN_PASSWORD: 'Acceptance-only-2026!' }
  : existsSync(envFile) ? Object.fromEntries(readFileSync(envFile, 'utf8')
    .split(/\r?\n/).map(line => line.match(/^([A-Z_]+)=(.*)$/)).filter(Boolean).map(m => [m[1], m[2]])) : {};
let token;
function positiveInteger(name, fallback, maximum) {
  const value = Number(process.env[name] || fallback);
  if (!Number.isSafeInteger(value) || value < 1 || value > maximum) throw new Error(name + ' must be between 1 and ' + maximum);
  return value;
}
function percentile(values, fraction) {
  return values.length ? +values[Math.min(values.length - 1, Math.ceil(values.length * fraction) - 1)].toFixed(2) : null;
}
async function call(path, options = {}) {
  const response = await fetch(base + path, { ...options, signal: AbortSignal.timeout(15000),
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}), ...options.headers } });
  if (!response.ok) {
    await response.arrayBuffer();
    throw new Error('HTTP ' + response.status);
  }
  return response.status === 204 ? null : response.json();
}
function validatePage(page) {
  if (!Array.isArray(page?.content) || !Number.isSafeInteger(page.totalElements) || page.totalElements < 0)
    throw new Error('Invalid search page response');
}
async function measure(scenario) {
  const path = '/api/products/search?' + new URLSearchParams({ q: '', page: '0', size: '20', ...scenario.params });
  let first;
  for (let i = 0; i < warmups; i++) {
    first = await call(path);
    validatePage(first);
  }
  if (!allowEmptyCatalogue && first.totalElements === 0)
    throw new Error('Workload "' + scenario.name + '" matches no products. Seed a representative catalogue first.');
  const samples = [], errors = {};
  let next = 0, failures = 0;
  const begun = performance.now();
  await Promise.all(Array.from({ length: Math.min(workers, requests) }, async () => {
    while (next++ < requests) {
      const started = performance.now();
      try {
        const page = await call(path);
        validatePage(page);
        if (page.totalElements !== first.totalElements) throw new Error('Catalogue changed during benchmark');
        samples.push(performance.now() - started);
      } catch (error) {
        failures++;
        const reason = error.name === 'TimeoutError' ? 'Timeout' : error.message;
        errors[reason] = (errors[reason] || 0) + 1;
      }
    }
  }));
  const durationMs = performance.now() - begun;
  samples.sort((a, b) => a - b);
  return { name: scenario.name, params: scenario.params, matchingProducts: first.totalElements,
    requests, workers: Math.min(workers, requests), warmupRequests: warmups,
    successes: samples.length, failures, errors, durationMs: +durationMs.toFixed(2),
    p50Ms: percentile(samples, 0.50), p95Ms: percentile(samples, 0.95), p99Ms: percentile(samples, 0.99),
    successfulRequestsPerSecond: +(samples.length / (durationMs / 1000)).toFixed(2) };
}
try {
  if (!settings.ADMIN_EMAIL || !settings.ADMIN_PASSWORD) throw new Error('Local benchmark credentials unavailable');
  const start = performance.now();
  const login = await call('/api/users/login', { method: 'POST', body: JSON.stringify({ identifier: settings.ADMIN_EMAIL, password: settings.ADMIN_PASSWORD }) });
  token = login.jwt;
  if (!token) throw new Error('Login did not return a session token');
  const loginMs = performance.now() - start;
  const catalogue = await call('/api/products/search?q=&availableOnly=false&page=0&size=1');
  validatePage(catalogue);
  const scenarios = suite ? [
    { name: 'browse-available', params: { availableOnly: 'true' } },
    { name: 'text-search', params: { q: 'Rice', availableOnly: 'true' } },
    { name: 'category-price', params: { category: 'grocery', minPrice: '100', maxPrice: '800', availableOnly: 'true' } },
    { name: 'nearby-radius', params: { latitude: '18.625156', longitude: '79.943906', radiusKm: '1', availableOnly: 'true' } },
    { name: 'fuzzy-full-name', params: { q: 'Rcie - Variant 1', fuzzy: 'true', availableOnly: 'true' } },
  ] : [{ name: 'catalogue-search', params: { q: query } }];
  const results = [];
  for (const scenario of scenarios) {
    const result = await measure(scenario);
    results.push(result);
    if (result.failures) process.exitCode = 1;
  }
  console.log(JSON.stringify({ timestamp: new Date().toISOString(), base, node: process.version,
    measurement: 'complete successful HTTP response including JSON parsing; errors excluded from percentiles',
    authentication: 'administrator session with downstream session validation', catalogueProducts: catalogue.totalElements,
    loginMs: +loginMs.toFixed(2), results }, null, 2));
} finally {
  if (token) await call('/api/users/logout', { method: 'POST' });
}
