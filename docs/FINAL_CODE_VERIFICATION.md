# Local verification

For the October 10 refresh-token, optional driving-distance and accessibility
changes, use [the newer feature verification](LOCAL_FEATURE_VERIFICATION.md).
The October 5 results below remain dated evidence for that pass.

## Latest local evidence: 2026-10-05

This is the main reference for the local portfolio demo. Counts describe
separate runs, not a cumulative suite or production certification.

| Check | Observed result |
| --- | --- |
| Angular production build | Passed |
| Frontend unit suite | 62 ChromeHeadless tests passed; final focused moderation rerun passed its 2 tests (not added to 62) |
| Focused backend suite | Rating Service 7 and backend-common 3 tests passed, with no failures, errors or skips |
| Isolated Compose and API smoke | Services healthy; OTP/account, approval, catalogue, social and session workflows passed |
| Browser acceptance | Five distinct checks passed across main and focused reruns, including a separate live Maps check |
| Dashboard visual review | Populated desktop (1440 × 1000) and mobile (390 × 844) screenshots inspected; no horizontal overflow observed |
| Seeded search benchmark | 1,000 measured requests, zero failures, 1,203 catalogue rows; workload p95 15.02–21.67 ms |
| Seed repeatability | Second seed run added zero products |

Browser results were obtained across reruns, not one final combined suite.
Generated backend reports live in each module's `target/surefire-reports`;
screenshots are in the ignored `artifacts/visual-acceptance` directory. Raw
benchmark JSON is linked below. The isolated stack was stopped after verification;
its data volume remains available for reuse.

Reproduce the focused backend run from the repository root:

```powershell
$env:LOG_FORMAT='ecs'
./UserService/mvnw.cmd -B -ntp -pl RatingService -am test
```

An initial run inherited unsupported `LOG_FORMAT=json` and failed startup. The
successful rerun used `ecs`, matching the configured structured-log format.

## Scope

Session listing, owned-session revocation and logout-all have backend regression
coverage. Sensitive profile changes invalidate sessions. Rotating refresh
tokens were added in the October 10 pass linked above.

Review reporting includes duplicate/own-review protection, admin-only decisions,
and persisted report snapshots. V3 retains reports after deletion of the source
review, fixing the original cascading-delete audit loss. Existing V2 migrations
are unchanged. The queue is pageable by status, defaults to 20 reports per page,
and validates page/size bounds. It orders by creation time and ID so equal
timestamps have a deterministic order.

## Isolated browser acceptance

Run from the repository root. This creates a separate database volume and network;
do not omit the project name. Requires Docker Compose with `!override` support.

```powershell
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml up -d --build --wait
$env:VISUAL_BASE_URL='http://localhost:18088'
$env:E2E_OTP_CODE='000000'
$env:E2E_OFFLINE_MAPS='true'
./scripts/smoke.ps1 -BaseUrl http://localhost:18080 -OtpCode 000000
Push-Location street-smart-frontend
npm.cmd run test:visual
Pop-Location
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml stop
```

The fixed OTP provider runs only in this test profile. Maps is disabled in the
isolated stack; provider validation is a separate external integration check.
Tests create disposable accounts in the isolated database.
This profile bootstraps its own fixed test administrator, so changing the normal
local administrator password does not break isolated acceptance.

## Local dashboard and moderation verification (2026-10-05)

- Angular build passed; 62 ChromeHeadless tests passed, including moderation
  recovery when resolving the final report removes the last page.
- Rating Service: 7 tests passed; shared backend: 3 tests passed. A regression
  inserts 105 reports and verifies the sixth page, total count and invalid bounds.
  This is a focused run, not a rerun of the full historical reactor count below.
- Isolated Compose services became healthy. Smoke checks passed for test OTP,
  registration/login, approval/rejection/resubmission, catalogue changes/search,
  reviews, favourites, moderation snapshot retention and logout revocation.
- Browser acceptance passed visible OTP registration/login, role pages at
  desktop/mobile sizes, image upload/download/delete, populated product results,
  filter reset, geolocation distance and the offline Maps fallback.
- Separately, the configured live Google Map loaded successfully with no
  detected provider authentication errors. Real SMS delivery was not exercised;
  OTP acceptance uses only the isolated test provider.
- Populated desktop/mobile screenshots are in
  `artifacts/visual-acceptance/dashboard-seeded-desktop.png` and
  `artifacts/visual-acceptance/dashboard-seeded-mobile.png`. Distances are
  labelled as straight-line estimates.

## Performance interpretation

`scripts/performance.mjs` defaults to one login and 100 catalogue-search
requests with four workers, reporting failures, p50/p95/p99 latency and successful
throughput. It reads and parses the complete response before stopping the timer;
failed requests are reported separately and excluded from percentiles.
`PERF_REQUESTS`, `PERF_WORKERS`, `PERF_WARMUPS` and `PERF_QUERY` configure
the bounded workload. The script reads local administrator credentials privately, creates one
session and logs it out. The target is restricted to localhost; set
`PERF_BASE_URL` to change its port.

The benchmark rejects workloads with zero matching products by default. Use the
seeded reproduction workflow below, or set `PERF_ALLOW_EMPTY_CATALOGUE=true` only when deliberately measuring
an empty-catalogue baseline. Output records the query, worker count and matching
catalogue size. This remains a local regression baseline, not a capacity claim or
a measurement of SMS delivery, image upload, or production traffic.

## Seeded local search baseline (2026-10-05)

The reusable API seed creates 12 approved fixture shops with 100 products each:
two shops per grocery, electronics, clothing, books, pharmacy and restaurant
category. Product names, prices, availability and nearby coordinates vary.
960 of the 1,200 fixture products are available. The measured catalogue contained
1,203 rows because three earlier acceptance listings remained in the isolated
database. The fixture uses synthetic data; it does not model every real shop's
inventory distribution.

The run used Node v24.13.1, Docker Desktop Linux containers and PostgreSQL 16.
Each workload performed ten warm-ups followed by 200 requests with four workers,
returning the first page of 20 results. Requests went through the web proxy and
gateway with a real admin session and downstream session checks. Seeding had
finished before measurements started.

| Workload | Matches | p50 ms | p95 ms | p99 ms | Successful requests/s | Failures |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Available products | 962 | 15.67 | 21.67 | 25.75 | 243.98 | 0 |
| Text: Rice | 40 | 12.64 | 15.02 | 21.13 | 307.07 | 0 |
| Grocery, INR 100–800 | 65 | 11.36 | 15.40 | 23.09 | 334.58 | 0 |
| Within 1 km of the demo coordinates | 402 | 12.52 | 16.54 | 20.38 | 303.75 | 0 |
| Fuzzy full name: Rcie - Variant 1 | 2 | 12.36 | 18.82 | 21.54 | 298.04 | 0 |

All 1,000 measured requests succeeded. Login took 136.07 ms, outside the search
timings. The fuzzy query uses a nearly complete name because the current
Levenshtein path compares whole product names. These are short warm-cache local
regression measurements; they do not establish sustained load limits or
deep-pagination performance. The historical empty-catalogue run below also used
a different timing method and is not a direct before/after comparison.

Raw measured output: [2026-10-05-catalogue.json](benchmarks/2026-10-05-catalogue.json).

Reproduce from the repository root:

```powershell
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml up -d --build --wait
node scripts/seed-performance.mjs
$env:PERF_E2E='true'
$env:PERF_SUITE='true'
$env:PERF_REQUESTS='200'
$env:PERF_WORKERS='4'
$env:PERF_WARMUPS='10'
node scripts/performance.mjs
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml stop
```

The seed is fixed to the isolated web port 18088 and test administrator from
`compose.e2e.yml`; it uses the fixed OTP provider. Repeating it reuses its own
shops and named products; the verification rerun added zero products. Fixture
data remains in the isolated volume. No normal local accounts or secrets are
changed. For a single-query run, set
`PERF_SUITE='false'` and `PERF_QUERY` to the desired product text.

## Historical full-reactor results (2026-09-21)

- Full nine-module Maven reactor: 65 tests passed with zero failures, errors or
  skips; formatting and packaging also passed.
- Angular production build passed; 60 ChromeHeadless tests passed, including
  anonymous, accepted-role and rejected-role route-guard checks.
- Node syntax validation passed for the parameterized performance script.

Historical benchmark from 2026-09-19: Node v24.13.1, empty catalogue,
100 requests / 4 workers,
  zero failures, p50 70.22 ms, p95 129.46 ms, 48.73 requests/second.
One login took 1856.35 ms. The old result is retained for provenance but is not
evidence of realistic search performance; the updated script would reject it by
default.
