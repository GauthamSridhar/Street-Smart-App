# Local feature verification — 10 October 2026

This supplements the dated October 5 portfolio evidence. It covers refresh tokens, optional driving estimates and a repeatable accessibility audit. Payments and orders are intentionally outside Street Smart's product-discovery scope.

## Refresh tokens

- Login sets an opaque refresh token in an **HttpOnly, SameSite=Strict** cookie scoped to `/api/users`. Only its SHA-256 hash is persisted. The access JWT remains the existing login response contract.
- `POST /api/users/refresh` requires the cookie and `X-Requested-With: StreetSmart`. Each successful refresh rotates the secret. Reusing a consumed token revokes that entire session, including its replacement token.
- Sessions have an absolute seven-day deadline; rotation does not extend it. Logout, logout-all and session deletion invalidate the linked refresh tokens.
- Angular renews expired access tokens before protected navigation, and retries a protected request once after a 401. Concurrent renewal requests within one tab share one refresh. A renewal arriving after local logout cannot restore that session.
- `REFRESH_COOKIE_SECURE` defaults to false for local HTTP; the HTTPS production Compose overlay sets it to true. Responses containing credentials use `Cache-Control: no-store`.

Limitations: independent tabs do not coordinate renewal. A simultaneous cross-tab refresh can trigger the conservative replay policy and require signing in again. Access JWTs still live in session storage; HttpOnly refresh cookies do not solve XSS. This is not a claim of a complete production identity platform.

## Driving distance

Shop details now offers an explicit, on-demand driving-distance lookup using the Google Maps JavaScript Routes library. It requests browser location permission, displays route kilometres and approximate minutes, and labels the estimate as excluding live traffic. Nearby discovery continues to use straight-line distance; it is not silently relabelled as road distance.

Invalid coordinates, missing keys, unavailable routes and timeouts produce an actionable fallback to **Open directions**. No route calls are made automatically for every search result. A UI timeout does not guarantee cancellation of a provider request already sent.

### Google setup and manual acceptance

1. In your Google Cloud project, enable **Maps JavaScript API** and **Routes API**, with the provider's required billing configuration. This can incur charges; no billing changes were made by this implementation.
2. Configure the existing `GOOGLE_MAPS_API_KEY` setting in the root `.env` for the containerized frontend. Restrict the browser key by HTTP referrer to your actual local app origins, and by the required APIs. A browser Maps key is public configuration, not a server secret.
3. Recreate/build the frontend container with the lab overlay so the runtime configuration receives the setting: `docker compose -f compose.yml -f compose.lab.yml up -d --build web` (local app at `http://localhost:8088`).
4. Open a shop with coordinates, select the driving-distance action and allow location access. Confirm road distance and approximate minutes appear; compare the destination with Open directions.
5. Deny location permission, disable the key or use an unreachable destination to confirm the fallback remains usable.

The route adapter has passing unit coverage for provider fields/unit conversion, missing routes, missing configuration and invalid coordinates. **A live Google Routes response was not verified in this run.** Do not describe mocked route data as a measured local driving distance.

Provider references: [Routes library setup and requests](https://developers.google.com/maps/documentation/javascript/routes/get-a-route), [Route API reference](https://developers.google.com/maps/documentation/javascript/reference/route).

## Accessibility audit

`npm run test:a11y` builds the real production Angular app and audits controlled, populated API fixtures using Playwright and axe-core. Coverage comprises 14 public/customer/shopkeeper/admin routes at desktop and mobile sizes (28 checks), a shop-dialog keyboard/accessibility check, and an expired-session browser check: **30 passing checks**.

The axe audit selects WCAG 2 A/AA, 2.1 A/AA and 2.2 AA rule tags and rejects any reported violation in that scope. It found and prompted a fix for the registration country-code selector's accessible name. Other changes include a keyboard skip link, focus-visible styling, login/register main landmarks, and a trapped shop dialog that closes with Escape and restores focus.

Evidence is written under `artifacts/accessibility/`, including a JSON report and attached axe results, and uploaded by the frontend CI workflow. This is a formal, repeatable **automated** audit plus a targeted keyboard regression, not WCAG certification. Human screen-reader testing, all browser/assistive-technology combinations, every error/empty state and a complete manual WCAG review remain outside the verified scope.

## Verification performed

| Check | Result |
| --- | --- |
| UserService tests, including refresh rotation/replay/expiry/logout | 21 passed |
| Shared backend tests | 3 passed |
| Gateway tests | 4 passed |
| Angular unit tests | 68 passed |
| Production frontend build | Passed |
| Accessibility/browser suite described above | 30 passed |
| Real-stack browser suite, including refresh cookie, account forms, role pages and catalogue | 5 passed; live Google Maps check intentionally skipped in offline mode |
| PostgreSQL + gateway refresh integration | Passed rotation, replay revocation, protected API denial, logout and logout-all |
| Isolated Docker stack build/readiness | All services healthy |
| Existing API smoke regression | Passed OTP/account, approval/rejection/resubmission, catalogue, reviews, favourites, moderation retention and logout checks |

These are focused current checks, not a new run of every historical benchmark. The frontend dependency installation also reported 22 audit findings (5 moderate, 14 high, 3 critical); dependency upgrades were not silently forced as part of this feature work.

### Repeat locally

From the repository root (PowerShell):

```powershell
$env:LOG_FORMAT='ecs'
./UserService/mvnw.cmd -B -ntp -pl UserService,DiscoveryClient -am spotless:check test
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml up -d --build --wait
node scripts/verify-refresh.mjs
```

From `street-smart-frontend`:

```powershell
npm.cmd test -- --watch=false --browsers=ChromeHeadless
npm.cmd run test:a11y
$env:VISUAL_BASE_URL='http://localhost:18088'
$env:E2E_OTP_CODE='000000'
$env:E2E_OFFLINE_MAPS='true'
npm.cmd run test:visual -- --grep 'real refresh cookie'
```

The integration script targets only the isolated acceptance stack and its deliberately public test administrator. Do not point it at a personal or production account. When finished, stop that isolated stack from the repository root without deleting its volumes:

```powershell
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml stop
```
