# Remaining-work verification

## Scope

Session listing, owned-session revocation and logout-all have backend regression
coverage. Sensitive profile changes already invalidate sessions. Refresh tokens
are not implemented; users sign in again after access-token expiry.

Review reporting includes duplicate/own-review protection, admin-only decisions,
and persisted report snapshots. V3 retains reports after deletion of the source
review, fixing the original cascading-delete audit loss. Existing V2 migrations
are unchanged. The queue currently returns the oldest 100 reports per status.

## Isolated browser acceptance

Run from the repository root. This creates a separate database volume and network;
do not omit the project name. Requires Docker Compose with `!override` support.

```powershell
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml up -d --build --wait
$env:VISUAL_BASE_URL='http://localhost:18088'
$env:E2E_OTP_CODE='000000'
$env:E2E_OFFLINE_MAPS='true'
Push-Location street-smart-frontend
npm.cmd run test:visual
Pop-Location
node scripts/performance.mjs
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml stop
```

The fixed OTP provider runs only in this test profile. Maps is disabled in the
isolated stack; provider validation is a separate external integration check.
Tests create disposable accounts in the isolated database.

## Performance interpretation

`scripts/performance.mjs` measures one login and 100 catalogue-search requests
with four workers, reporting failures, p50/p95 latency and throughput. It reads
local administrator credentials privately, creates one session and logs it out.
The target is restricted to localhost. Set `PERF_BASE_URL` to change its port.
Results include catalogue size: empty-catalogue timings do not establish search
scalability. This is a local regression baseline, not a capacity claim or a
measurement of SMS delivery, image upload, or production traffic.

## Observed local results (2026-09-19)

- User Service: 19 tests passed; Rating Service: 6 tests passed; shared backend: 3 tests passed.
- Frontend production build passed; 57 ChromeHeadless tests passed.
- Benchmark: Node v24.13.1, empty catalogue, 100 requests / 4 workers,
  zero failures, p50 70.22 ms, p95 129.46 ms, 48.73 requests/second.
  One login took 1856.35 ms. Other local containers and tests were running;
  these values describe this run only.
