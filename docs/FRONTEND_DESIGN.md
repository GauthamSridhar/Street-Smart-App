# Product-first discovery and frontend design

The application retains its Angular workflows and product-first responsibility
boundaries. The September 13 visual refresh builds on the existing ivory,
charcoal and amber palette with a neighbourhood illustration, clearer product
cards, consistent forms, and responsive navigation.

## Visual design and motion

- The landing page uses a decorative neighbourhood illustration, not live map
  data. Its calls to action use existing registration and login routes.
- Search filters retain their existing bindings, URL state and event handlers.
  Results use two columns on larger screens and one on phones, with availability,
  shop details and price kept visible. Similar-name matching remains opt-in.
- Shared styles provide consistent surfaces, input sizes, keyboard focus and
  authentication panels across customer, shopkeeper and administrator screens.
- Short entrance animations and hover feedback respect reduced-motion settings;
  content never depends on scrolling or animation to become accessible.
- No new UI libraries, API changes or business-logic changes were introduced.
  Browser acceptance runs against the Docker-served frontend at both desktop and
  phone widths.

Verification for this refresh: 56 ChromeHeadless tests passed. The final Docker
production build passed without budget or CSS warnings (480.40 kB initial raw
bundle). The local web container was rebuilt with the updated frontend.

### Theme consistency follow-up

Customer, shopkeeper and administrator pages now share left-aligned page headers,
supporting descriptions, toolbar spacing and empty-state styling. Registration
and shop registration share the authentication layout. OTP and rejection dialogs
use the same bounded, scrollable surface; shop-detail tabs and icon actions use
shared keyboard-focus styles. Loading indicators use amber, while success,
warning and error states retain their semantic colours. About and Error include
the shared navigation, and pale illustrations use a dark supporting surface.

Use the `ss-*` classes in `src/styles.css` for new screens. Keep page content in
`ss-page` / `ss-shell`, headings in `ss-page-header`, and use the existing input,
button, badge, alert, dialog and tab variants instead of isolated styles. Reserve
the dark treatment for navigation, authentication and marketing sections.

The follow-up passed all 56 ChromeHeadless tests and a production build without
warnings (480.40 kB initial raw bundle).

## Browser acceptance

`npm run test:visual` uses the installed Chrome executable against the Docker
web service. It captures and checks 1440 x 1000 and 390 x 844 layouts for the
landing, login, registration, customer dashboard, favourites, profile,
shopkeeper registration/dashboard/products/reviews, and administrator
dashboard/requests. It verifies a real register-and-login journey, role routing,
visible page headings and the absence of horizontal overflow. Screenshots are
written to the ignored `artifacts/visual-acceptance` directory for review.

The suite uses fresh local demo accounts and the local administrator configured
in `.env`; it never sends data outside the development stack. For an OTP-enabled
browser run, start the explicit E2E overlay and use its deterministic local-only
code. The `e2e` Spring profile replaces Twilio only in that container, so this
command must not be used for normal local work or deployment:

```powershell
$env:E2E_OTP_CODE = '000000'
docker compose -p street-smart-e2e -f compose.yml -f compose.lab.yml -f compose.e2e.yml up -d --build --wait
$env:VISUAL_BASE_URL = 'http://localhost:18088'
$env:E2E_OFFLINE_MAPS = 'true'
$env:E2E_OTP_CODE = '000000'
Set-Location street-smart-frontend
npm run test:visual
```

The browser suite completes send, verify and registration with that code. The
Google Maps acceptance check remains separate and requires the configured
browser Maps key.

## Primary journey

Search for a product -> compare matching shop inventories -> inspect a shop ->
get directions, save the shop, or write a review.

Customers search **product names**, not shop names or descriptions. Results are
product/shop pairs, so the same product stocked by two shops produces two
results. Category means the shop's category; products do not yet have their own
category taxonomy. Available-only is the default. Inactive shops are labelled
explicitly, and inventory availability does not imply opening hours or reserve
stock. Pending/rejected shops never enter discovery results.

An empty query browses approved shops' catalogues. Default matching is a
case-insensitive literal substring; SQL wildcard characters are escaped.
Optional typo tolerance also matches full product names within two edits,
requiring at least three query characters. It is not semantic or synonym search.
INR price and radius filters are applied in the database before pagination.
Categories come from visible shop catalogues. Owner-provided hours are displayed
as local-time text; the app does not infer holiday schedules or an open-now flag.

## Responsibility boundaries

| Concern | Owner |
| --- | --- |
| Product filtering, visibility, total count and pagination | Shop Service `/api/products/search` |
| Query, category, availability and page in the URL | Dashboard |
| Debounce, immediate cancellation and error/retry state | ProductSearchState |
| Typed HTTP payloads | Domain API services |
| Token attachment and protected-request 401 handling | AuthInterceptor |
| Session expiry and role-based navigation | SessionService / AuthGuard |
| Map markers and result context | Optional MapComponent |
| Directions | External Google Maps link, independent of map configuration |
| Authoritative permission checks | Backend services, never browser storage |

The query is shareable and survives refresh. Changing filters resets the page;
all filters are sent together. Search cancels an old request as soon as a new
query arrives, including during the debounce interval. Shop selection also
cancels outdated detail requests. Loading, empty, error and retry states are
distinct. Pagination operates over the database, not a downloaded first page.

Results are lightweight projections: they do not hydrate whole shop catalogues
or image bytes. The map deduplicates shops from the current result page. Product
search and shop details work without a Maps key or location permission. No
distance/radius ordering is claimed in this implementation.

## Authentication and API contracts

- A single registered interceptor adds tokens only to the configured API's
  origin and path. It never adds tokens to Maps or other third-party requests.
- Expired/malformed sessions lose their cached identity and shop context.
  A late 401 from an old session cannot log out a newer session. Browser guards
  improve navigation; the backend remains the security boundary.
- Shopkeepers load their owned shop using `/shops/owner/{id}`. Missing shops
  return 404, not a fabricated registration condition inferred from a 500.
- Profile/shop updates include only editable fields. Product updates include
  both the name and intended availability. Review bodies omit user metadata.
- SMS capability comes from `/sms/config`. Enabled verification uses server
  send/verify/proof endpoints; disabled environments visibly create unverified
  accounts. There is no email-verification feature or client-side OTP bypass.
  Changing the phone number invalidates the local proof for the old number.
- Review totals/averages use the summary API. `/ratings/mine/{shopId}` finds
  the caller's review independently of pagination. Dates use backend timestamps;
  customer identities and photos are not invented.
- Image downloads use authenticated HTTP blob requests. Object URLs are revoked
  and in-flight downloads are cancelled when the image component is destroyed.
- Administrator decisions are shown as saved with delivery pending when
  applicable. Dashboard counts come from real APIs; unavailable historic/user
  metrics are not filled with demo numbers.

## Local operation

Start the backend using the root README, then:

```powershell
cd street-smart-frontend
npm ci
npm start
```

Open `http://localhost:4200`. The Angular development proxy sends `/api` requests
to the gateway at `http://127.0.0.1:8080`. Do not enable broad CORS to bypass
configuration issues. Production hosting must route `/api` to the gateway or
provide a correct public API base URL with an explicitly allowed frontend origin.

`public/config.js` contains only public settings. An empty Maps key disables
the map gracefully. Compose generates it at runtime from `GOOGLE_MAPS_API_KEY`
in the ignored root `.env`. To use Maps, configure a fresh browser key restricted by
HTTP referrer and the required APIs. Never place JWT, database or SMS secrets
in frontend configuration. The old committed browser key still needs external
restriction/rotation; removing it from source does not revoke it.

```powershell
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

See Angular's [interceptor guide](https://angular.dev/guide/http/interceptors)
and [HTTP testing guide](https://angular.dev/guide/http/testing) for the framework
patterns used by the authentication and API tests.

## Current delivery and remaining boundaries

Angular 21 uses the lighter Angular builder; the dated verification report records
build/test/audit results. Shopkeepers manage images, richer products, opening-hour
text and rejected-shop resubmission. Logout calls the server before clearing the
local session, and sensitive profile changes require sign-in again.

PostgreSQL has a trigram index for substring queries and a coordinate prefilter
for radius queries. Query cost and geographic scale still need measurement;
there is no PostGIS claim. Per-product taxonomy, synonyms and automatic opening
schedules are future product decisions. Manual browser acceptance, screenshots,
provider credential revocation and an actual AWS deployment remain unverified.
