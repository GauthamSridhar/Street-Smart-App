# Street Smart

Street Smart helps customers find products stocked by local shops, with optional
map discovery, catalogues, reviews and favourites. Shopkeepers manage their shops;
administrators review registration requests.

## Supported backend

Java 17, Spring Boot 3.5, Spring Cloud 2025.0, PostgreSQL and an Angular frontend.
The root Maven reactor builds the implemented backend and its shared infrastructure.

| Module | Responsibility | Internal port |
| --- | --- | --- |
| EurekaServer | Service discovery | 8761 |
| DiscoveryClient | Gateway, JWT verification, explicit-origin CORS | 8080 |
| UserService | Accounts, login, phone verification | 8082 |
| ShopService | Shops, products, image storage, discovery search | 8083 |
| ShopApprovalService | Administrator decisions and delivery tracking | 8084 |
| RatingService | Reviews and rating summaries | 8085 |
| FavoriteService | Idempotent favourites | 8086 |
| backend-common | Security, API errors, metrics, client configuration | Library |

Only the gateway is published on the host by Compose. Each domain owns its
database. Products belong to Shop Service; SMS belongs to User Service.

## Run locally

Requires Docker with Compose and roughly 4 GB of available Docker memory.
Java 17 is additionally required for tests outside Docker. PowerShell 7 works
on Windows/Linux; Windows PowerShell is also supported.

```powershell
./scripts/backend.ps1 -Action Init
./scripts/backend.ps1 -Action Start
./scripts/smoke.ps1
```

`Init` generates an ignored `.env` with random local secrets. It does not
overwrite an existing file. The administrator email/password are in that file;
there is no public administrator registration endpoint. The gateway is
`http://localhost:8080`. Eureka registration can take a short time after
containers become healthy.

`Stop` stops containers without deleting their databases:

```powershell
./scripts/backend.ps1 -Action Stop
```

Compose provisions fresh databases. The original developer databases are not
automatically migrated. Flyway deliberately refuses to adopt an existing
unmanaged schema: back up existing data, reconcile it with the migrations and
perform a reviewed migration before reusing an older database. Do not enable
automatic baselining or delete a volume to hide a schema error.

## Build and verification

Session/moderation regression checks, isolated OTP browser acceptance and the
local performance baseline are documented in [final code verification](docs/FINAL_CODE_VERIFICATION.md).

See [the dated verification report](docs/BACKEND_VERIFICATION.md) for observed
test results, verified workflows and explicit limitations.

```powershell
./scripts/backend.ps1 -Action Test
./scripts/backend.ps1 -Action TestPostgres
```

The first command builds all active modules and runs security/workflow tests
using isolated H2 test databases. The second runs the same domain-service tests
against PostgreSQL in an isolated, temporary container on localhost:55441.
It never connects to the development databases. Test reports are in each
module's `target/surefire-reports`.

The Maven wrapper is currently located in UserService; from the repository root:

```powershell
./UserService/mvnw.cmd -B -ntp verify
```

On Unix use `bash UserService/mvnw -B -ntp verify`. CI builds and tests the
whole reactor and repeats the domain tests on PostgreSQL.

## Security and data ownership

Every domain service verifies signed JWTs, issuer, expiry and audience. A stable
Base64-encoded key comes from `JWT_SECRET`; restarting a service no longer
invalidates tokens. Access tokens last one hour by default. There is currently
no refresh token: sign in again after expiry. Login persists a session; Compose
domain services check it through User Service using `SESSION_VALIDATION_URL`.
Logout revokes that session, and sensitive profile changes revoke all sessions
in the same database transaction. Session validation fails closed on outages;
this adds User Service availability and a bounded HTTP lookup to protected APIs.
Standalone runs must configure that URL to enable cross-service revocation.

Ownership comes from the signed subject, not a client-provided identifier.
Public registration accepts only USER and SHOPKEEPER. Administrator bootstrapping
uses environment variables and never promotes an existing non-admin account.
Sensitive profile edits require the current password.

Internal approval endpoints use a separate service credential and are denied
at the gateway. Domain ports and Eureka are private to the Compose network.
All services currently share the local signing key and internal credential;
use separate workload identities/asymmetric signing when crossing independent
trust boundaries.

Ratings and favourites are owned by their respective services. They do not
rewrite user or shop profiles. Unique constraints prevent duplicate customer/
shop pairs. Repeating a favourite add keeps the favourite; it does not toggle
it off. Removing an absent favourite succeeds.

Shop registration commits a pending delivery flag with the shop. A worker
delivers the approval request and retries failures. Administrator decisions
similarly commit an explicit status, actor, reason and pending delivery flag.
The recipient is idempotent, so a successful HTTP call followed by a database
rollback can be retried safely. Decisions converge after the next worker cycle;
an HTTP success records the decision, not a claim that every service has already
observed it.

## API notes

- Login: `POST /api/users/login`; current profile: `GET /api/users/me`.
- Token validation: `POST /api/users/validate` with an Authorization header.
- Shop search: `GET /api/shops/search?page=0&size=20&q=&category=`.
- Customer product discovery: `GET /api/products/search?q=milk&category=Grocery&availableOnly=true&page=0&size=20`.
  Product names are matched literally, case-insensitively by default. Optional
  `fuzzy=true` includes full names within two character edits (minimum three
  query characters). Price ranges default to INR; coordinates and `radiusKm`
  filter by distance before pagination. Results identify each
  matching shop; pending/rejected shops are excluded. Categories and availability
  combine with the query, and pagination is performed by the backend.
- The legacy shop list returns at most 100 shops. Customers see approved shops;
  shopkeepers see their own shop; administrators see all statuses.
- Rating summary: `GET /api/ratings/summary/{shopId}`.
- Images: JPEG/PNG only, maximum 5 MB and 16 megapixels. Image content is checked
  server-side and filenames are generated. PostgreSQL storage is a deliberately
  simple portfolio deployment choice; object storage is a future extension.
- Validation failures return 400, missing authentication 401, forbidden actions
  403, missing records 404, conflicts 409, request limits 429, and dependency
  outages 503. Responses avoid leaking internal exception messages.
- Health: `/actuator/health`. Domain OpenAPI docs and metrics require an ADMIN
  token and direct private-network access; they are not exposed by the gateway.

## Phone verification and frontend integration

SMS is disabled by default, so local registration creates unverified accounts.
Set `SMS_ENABLED=true`, `SMS_ACCOUNT_SID`, and `SMS_VERIFY_SERVICE_SID`, then
supply either `SMS_AUTH_TOKEN` or `SMS_API_KEY_SID` plus `SMS_API_KEY_SECRET`
to enable real verification through Twilio Verify. `SMS_ACCOUNT_SID` always
starts with `AC`; API key SIDs start with `SK`; Verify Service SIDs start with
`VA`. `SMS_FROM` is not used by Verify. Credentials must be freshly issued; do
not reuse values previously committed in Git.

1. `POST /api/sms/send` with only `phoneNumber` in international format.
2. `POST /api/sms/verify` with `phoneNumber` and `otpCode`.
3. Include the returned `verificationToken` as `phoneVerificationToken` when
   registering or changing a phone number.

The backend generates codes, stores hashes, expires them after five minutes,
limits failed attempts, and consumes verification proof once. There is no
client-generated code or universal bypass. PostgreSQL holds a shared quota of
30 POST requests per minute for each authentication/OTP endpoint across replicas.
This is a conservative global quota, not a per-customer fairness policy.

The frontend uses this secure OTP contract, reads `/api/sms/config` to explain
disabled verification, handles missing owned shops and expired sessions, and
sends explicit editable payloads. It no longer relies on profile-level
rating/favourite lists. Product discovery works without Maps configuration.

Run `npm ci` and `npm start` inside `street-smart-frontend`, then open
`http://localhost:4200`. See [frontend design](docs/FRONTEND_DESIGN.md) for
search semantics, API ownership, configuration and remaining limitations.

## Configuration and operations

Run `./scripts/backend.ps1 -Action Init` once to create the single private
`.env` file, then edit only that file for local settings. `compose.yml` defines
the topology and `improvements.md` contains the remaining roadmap. Production
should terminate TLS, supply secrets from the hosting platform, restrict origins, back up PostgreSQL,
set resource limits and observe pending delivery flags. The current Compose
bootstrap database role is shared. After migrations, `scripts/database-roles.ps1`
generates the private `.env.roles` file and `compose.roles.yml` provides separate
runtime logins without schema-creation privileges. Do not manually edit
`.env.roles`; load both files when starting the restricted runtime:
`docker compose --env-file .env --env-file .env.roles -f compose.yml -f compose.lab.yml -f compose.roles.yml up -d --wait`
for the local lab frontend, or omit `compose.lab.yml` when serving only the API.
Migrations remain a separate deployment step.

For occasional AWS use, follow the [step-by-step AWS deployment guide](docs/AWS_DEPLOYMENT_GUIDE.md).
It uses one EC2 instance with an automatic stop timer and a private SSM tunnel.
No always-running load balancer, NAT Gateway or RDS is provisioned. Stopped EBS
storage still costs.
See [the portfolio case study](docs/CASE_STUDY.md), [the detailed demo notes](docs/PORTFOLIO.md) and
[security operations](docs/SECURITY_OPERATIONS.md) for the handoff.

The historical Git commits contain credentials. Removing them from current
files does not revoke them: rotate provider/database credentials and coordinate
any history rewrite with collaborators. No history rewrite is performed by the
backend setup.

Security implementation follows the
[Spring Security JWT resource-server documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html);
framework compatibility is listed in
[Spring Cloud's release matrix](https://spring.io/projects/spring-cloud/).
