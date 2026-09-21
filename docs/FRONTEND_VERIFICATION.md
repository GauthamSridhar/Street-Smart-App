# Frontend design verification - 2026-09-10

Scope: product-first discovery and correct frontend/backend contracts. Existing
styling was retained; this is not a visual redesign or a production certification.

This is a dated 10 September snapshot. A later 13 September visual acceptance
run covered desktop and mobile routes, and Twilio Verify was enabled for a
separate real-provider test; see `FRONTEND_DESIGN.md` and
`BACKEND_VERIFICATION.md` for those scoped updates.

## Verified locally

| Check | Result |
| --- | --- |
| Angular production build (`npm run build`) | PASS; initial bundle approximately 459 kB, within configured budget |
| Angular Karma / ChromeHeadless suite | PASS: 56 tests |
| Root Maven `spotless:apply verify` | PASS: 56 tests across the active reactor |
| `scripts/backend.ps1 -Action TestPostgres` | PASS: 49 distinct domain tests on PostgreSQL |
| `docker compose up -d --build --wait` | PASS; updated services healthy |
| `scripts/smoke.ps1 -BaseUrl http://localhost:4200` | PASS through the Angular development proxy |
| Frontend HTTP and proxied `/api/sms/config` | PASS; HTTP 200, SMS disabled in this local configuration |

Backend and PostgreSQL counts overlap; do not add them as distinct coverage.
The common-library tests also rerun during individual PostgreSQL module runs.
The smoke script creates demo users, shops and inventory in the local database.

The smoke flow covers registration/login, administrator decisions and delivery,
product updates, product-name search with combined category and availability
filters, reviews including the signed caller's review lookup, and favourites.
Frontend regression tests cover HTTP payloads, token scoping, session expiry,
late-401 handling, cancellation/retry, server OTP verification, map fallback and
directions without map/location prerequisites. Some component tests remain
creation checks; the count is not a coverage percentage.

## Reproduce frontend checks

From `street-smart-frontend` on Windows:

```powershell
npm ci
npm run build
$env:CHROME_BIN = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
npm test -- --watch=false --browsers=ChromeHeadless
npm start -- --host 127.0.0.1
```

With the backend running, open `http://localhost:4200`. From the repository root:

```powershell
.\scripts\smoke.ps1 -BaseUrl http://localhost:4200
```

## Manual browser acceptance checklist

Interactive browser controls were unavailable in this session. The checks above
are automated component/API checks, not a completed visual or click-through audit.

1. Register a customer and shopkeeper. With SMS disabled, confirm verification
   is explicitly unavailable rather than claimed successful.
2. Register a shop, approve it as administrator, refresh the shopkeeper status,
   and create a product. Confirm pending/rejected shops are absent from search.
3. Search by the product name, combine category and availability filters, change
   pages, and refresh the URL. Searching solely by shop name should not match an
   unrelated product. Mark inventory unavailable and test both filter settings.
4. Open product results and shop details. Try directions without configuring Maps
   or granting location permission; it should open an external Google Maps tab.
5. Add/edit a review and save/remove a favourite. Reopen the shop to confirm the
   persisted state. Check profile edits and role-specific routes after logout/login.
6. Verify loading, empty, retry/error states and keyboard operation. Test external
   Maps, image downloads and real SMS separately with their required configuration.

## Remaining limits

- No hosted CI run or production deployment is claimed; the frontend workflow was added.
- This is the historical 2026-09-10 result. The subsequent Angular 21 upgrade,
  search/workflow changes and security audit are recorded in RELEASE_VERIFICATION.md.
- The removed committed Maps key needs external restriction/rotation.
- In this dated snapshot, real SMS delivery and configured interactive Maps were
  unverified. The later scoped update records one Twilio Verify dispatch and
  automated desktop/mobile journeys; hosted production behaviour remains
  unverified.
- The newer product search adds opt-in typo tolerance and radius filtering.
  Category still describes the shop, not an independent product taxonomy.
- See [frontend design](FRONTEND_DESIGN.md) and [improvements](../improvements.md)
  for deferred workflows and production hardening.
