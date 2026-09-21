# Backend verification

Verified locally on 10 September 2026 with Java 17.0.18, Maven 3.9.9,
PostgreSQL 16.15 and Docker Compose. These are observed local results,
not a production certification or a claim that hosted CI has run.

Update on 13 September 2026: User Service tests passed after migrating the SMS
transport to Twilio Verify. A real Verify send returned HTTP 200 to one
Twilio-trial verified recipient. This confirms the local provider integration,
not unrestricted public SMS delivery or a production sender setup.

## Results

| Check | Result |
| --- | --- |
| Clean Maven reactor, packaging and formatting | PASS, all 9 modules |
| Full regression suite | 49 tests, zero failures/errors/skips |
| PostgreSQL domain suites | 42 domain tests, zero failures/errors/skips |
| Compose configuration and image builds | PASS |
| Development stack readiness | All 8 containers healthy |
| Gateway workflow smoke test | PASS |
| Live authentication, CORS and internal-route checks | PASS |
| Git whitespace check and private environment exclusion | PASS |

The full suite comprises common infrastructure (2), Eureka (1), gateway (4),
users (17), shops (11), approvals (6), ratings (4) and favourites (4).
PostgreSQL verification repeats the five domain suites against isolated test
databases on port 55441; common infrastructure tests also run with each reactor.
Reports under each module's `target/surefire-reports` describe its latest run.

## Verified behaviour

- Gateway rejects missing/invalid bearer tokens with 401. Configured-origin
  preflight succeeds; an untrusted origin returns 403. Even an administrator
  token cannot access internal service routes through the gateway (403).
- Actual Feign request construction attaches the internal key to internal
  target paths and forwards the user bearer token on public service calls.
- Public administrator self-registration and forged resource ownership are
  rejected. Wrong-issuer, wrong-audience and expired JWTs are rejected.
- OTP failed attempts persist, codes expire, resend is restricted, and
  verification proof is single-use. Twilio Verify calls are mocked in these
  tests; the dated update above records the separate real send check.
- Shop approval and rejection both reach Shop Service through the real
  scheduled delivery path. Unit/integration checks simulate delivery failures
  and confirm that retryable state is retained and exact decisions are retried.
- Product updates use supplied values. Rating validation/uniqueness and
  favourite ownership/idempotency pass. Repeating a favourite add through
  the running gateway leaves one record.
- Valid PNG upload/download/deletion works on H2 and PostgreSQL; invalid
  uploads and unauthorized deletion are rejected. Image bytes live separately
  from metadata.

## Reproduce

```powershell
./UserService/mvnw.cmd -B -ntp clean verify
./scripts/backend.ps1 -Action Init
./scripts/backend.ps1 -Action TestPostgres
./scripts/backend.ps1 -Action Start
./scripts/smoke.ps1
```

For intentional Java formatting changes, run
`./UserService/mvnw.cmd -B -ntp spotless:apply` before verification.

The development gateway is `http://localhost:8080`. Other development services
are private to Compose. The separate test database is loopback-only on 55441.
Smoke tests leave clearly named demo accounts and shops in the development
databases for inspection. `./scripts/backend.ps1 -Action Stop` stops the
development stack without deleting its data. No unrelated containers or old
developer databases were changed.

## Assessment and limits

Backend portfolio readiness: **8/10**, a subjective assessment based on the
verified security, domain correctness, tests and reproducible local operation.
This does not re-rate the unchanged frontend or mean the system is ready for
unrestricted public production traffic.

Remaining release work includes credential rotation, frontend API integration,
TLS/hosting configuration, backup/restore testing, least-privilege database
roles, load measurements and operational monitoring. Current JWTs have no
per-session revocation; shared local service keys and in-process throttling
are documented deployment tradeoffs. Real SMS delivery, historical data
migration, browser end-to-end behaviour and failure/load testing under
multiple replicas were not verified. Twilio Verify dispatch was checked once to
a trial-verified recipient; production SMS compliance remains unverified. See
[the roadmap](../improvements.md).
