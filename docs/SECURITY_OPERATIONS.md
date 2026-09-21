# Security and operations handoff

## Previously committed credentials

Twilio Verify is the optional SMS verification provider. Google Maps is the
optional embedded-map provider. The application works with `SMS_ENABLED=false`
and an empty Maps key. These settings avoid invoking those integrations; they do
not revoke historical keys. Identify the issuing accounts and revoke/rotate any
historical values in those provider consoles. If the values came from someone
else's starter code, do not attempt to use them. Database/JWT/internal deployment
secrets must be newly generated for each environment. No provider rotation or
Git history rewrite has been performed here.

The security CI workflow runs a redacted secret scanner and dependency checks.
Historical findings are expected until accounted for; do not hide them with a
broad allowlist. A clean dependency scan is not a penetration test.

## Runtime operations

Domain services emit ECS JSON logs with request IDs and expose authenticated
Prometheus metrics. Distributed tracing is not bundled in the runtime baseline;
add an OpenTelemetry exporter, collector endpoint and sampling policy before
claiming collected traces. Never send tokens, OTPs or request bodies into logs.
The existing request identifier remains useful without a collector.

`street_smart_approval_requests_pending` and
`street_smart_approval_decisions_pending` expose pending deliveries. Alert rules
are in `infra/alerts.yml`. Load them into a private Prometheus installation and
connect an alert receiver before claiming notifications are operational.
Metrics currently require an ADMIN bearer token and private-network access;
short-lived tokens require an authenticated scrape provisioning strategy.

For a backlog, check downstream health, Eureka registration, matching internal
keys and worker exceptions. Do not manually clear pending flags to silence an
alert. Retry delivery after fixing the dependency; revision checks keep retries
safe across resubmission.

## Sessions and scaling limits

Current policy uses expiring access tokens and explicit sign-in after expiry.
There are no refresh tokens. Login creates a persisted session, logout removes
it, and sensitive profile changes revoke all of the user's sessions atomically
with the account update. Login and account edits serialize on the user row.
Compose configures `SESSION_VALIDATION_URL`; every domain validates the session
through a private User Service endpoint. Outside Compose, configure that URL
explicitly. Blank configuration retains stateless validation for isolated tests.
There is no validation cache: revocation takes effect on subsequent validation,
not on requests already in flight. User Service failure denies protected access.

Authentication quotas are shared in PostgreSQL: 30 POST requests per endpoint
per minute, globally across replicas. This bounds auth/SMS traffic without
trusting spoofable forwarding headers. Per-customer fairness and provider SMS
spend controls require a separately defined trusted-proxy policy.

After migrating the base stack, run `scripts/database-roles.ps1` and start the
runtime with `docker compose --env-file .env --env-file .env.roles -f compose.yml
-f compose.lab.yml -f compose.roles.yml up -d --wait` for the local lab, or omit
`compose.lab.yml` when no Caddy lab frontend is needed. Protect `.env.roles`
like `.env` (mode 600 on Linux). Each runtime login can perform DML only in its
own database and cannot write Flyway history. On upgrades, stop runtime
instances, migrate with the bootstrap role using the base configuration, reapply
grants for new tables, then start the restricted runtime overlay. Do not disable
Flyway to skip needed migrations: Hibernate schema validation still verifies the
runtime schema.

## Backups and public deployment

`scripts/backup.ps1` creates custom-format PostgreSQL dumps without streaming
binary bytes through PowerShell text conversion. Encrypt copies off-host and
practice restoring into an isolated database. Per-database dumps are not a
single atomic snapshot of the whole application.

`compose.production.yml` adds TLS termination and resource limits. It requires
an actual domain and DNS configuration. `compose.lab.yml` instead uses an SSM
tunnel for an occasional private demo. Neither template proves a successful
deployment. See `AWS_LAB.md` for costs, start/stop and deletion boundaries.
