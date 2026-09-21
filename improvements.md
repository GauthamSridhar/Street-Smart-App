# Street Smart: Remaining Improvements

## Current pass: 2026-09-13

The original review below is historical. Current work spans all eight requested
areas; account actions and manual acceptance are not represented as completed.

| Area | Current delivery / remaining boundary |
| --- | --- |
| Credentials | Removed remaining Maps-key file; history scan found three findings. Twilio Verify was configured and its trial send path was tested locally; provider revocation still needs the issuing accounts. Maps remains optional. |
| Browser QA | Automated desktop/mobile visual acceptance, frontend/API checks and production build were run locally. Manual acceptance in the user's own browser remains a separate handoff step. |
| Dependencies | Angular 21 and lighter build tooling; npm audit reports zero findings. Backend vulnerability audit is recorded separately from unit tests. |
| Deployment | On-demand AWS EC2 template, start/stop helper, private SSM access, two-hour stop timer, TLS alternative, restricted database roles, backup/restore scripts. No AWS resources created. |
| Search | Price/currency and radius filtering, opt-in bounded typo matching, PostgreSQL trigram index, catalogue-derived categories and owner-provided opening hours. No automatic holiday/open-now calculation or scale claim. |
| Shopkeeper workflows | Image upload/delete/preview, product descriptions/prices/freshness, revision-safe approval resubmission and history. |
| Portfolio | Case study, architecture diagram, demo script, honest resume bullets, local visual-acceptance screenshots and operational limitations in docs/PORTFOLIO.md. Hosted-production screenshots remain pending deployment. |
| Operations | Persisted sessions and revocation, shared auth quotas, JSON logs, pending-delivery metrics, dashboard and alert definitions. Distributed tracing and alert delivery need deployment configuration. |

See `docs/AWS_LAB.md` for occasional-use cost boundaries and
`docs/SECURITY_OPERATIONS.md` for operational procedures.

## Original review and first-pass history

The initial review rated the repository 5.5/10 as a capstone prototype. The
backend work below addresses its major security and correctness findings;
the frontend and deployment concerns should be assessed separately.

## Backend changes implemented in this pass

- One Java 17 / Spring Boot 3.5 / Spring Cloud 2025.0 reactor for the five
  implemented domain services, gateway, registry and shared backend module.
- Stable JWT signing configuration and local signature, issuer, expiry and
  audience checks in every domain service and the gateway.
- Server-side ownership/role checks, public ADMIN registration blocked,
  password confirmation for sensitive profile changes, and private internal APIs.
- Twilio Verify-generated OTPs with local failed-attempt limits, resend cooldown,
  single-use proof and an explicitly disabled default SMS mode.
- Explicit approval/rejection transitions, administrator audit information and
  persisted retryable delivery between Shop and Approval services.
- Product updates apply supplied values; favourites are idempotent; unique
  constraints prevent duplicate favourites and ratings; social data no longer
  causes writes to user/shop profiles.
- Input validation, consistent error responses, restricted CORS, bounded client
  timeouts, authenticated metrics/docs and request identifiers.
- Flyway baselines, separate databases, a Compose environment, repeatable
  verification scripts, CI, dependency update configuration and Java formatting.
- Verified JPEG/PNG uploads, generated filenames, size/dimension limits and
  separate image metadata/content storage.
- Backend regression tests for security, workflows and failure recovery.

See the root README for commands, API contracts and operational limitations.
[The verification report](docs/BACKEND_VERIFICATION.md) records the passing
49-test reactor, PostgreSQL suites and running-stack smoke checks. Backend
portfolio readiness was assessed at 8/10 in that backend-only handoff.
The subsequent frontend design work is described below; production-readiness
limitations remain separate concerns.

## Required account/deployment actions

1. Rotate previously committed Twilio/database credentials and restrict the
   browser Maps API key. Deleting credentials from current files does not
   revoke them or remove historical copies.
2. Coordinate any Git history rewrite with collaborators. No history was
   rewritten during this pass.
3. Before public hosting, configure TLS, private service networking, backups,
   resource limits, fresh secrets, explicit browser origins and alerting.
4. Existing unmanaged databases need a reviewed migration/backfill. This is not
   applicable to the current fresh project with no prior data. If historical
   data is introduced later, the Flyway baselines intentionally do not silently
   adopt or erase it.
5. Check framework support windows against Spring's release matrix before
   public deployment; plan the next major upgrade separately from feature work.

## Frontend design and integration pass

- Product-first server search replaces shop-name filtering. Text, category and
  availability combine, queries persist in the URL, and stale requests cancel.
- Search is independent of maps/location permission and lists actual product/
  shop matches with pagination, loading, empty and retry states.
- Central session/interceptor handling, explicit API payloads, server OTP proof,
  an owned-shop dashboard, functional profile/detail routes and live admin counts
  replace the incompatible client assumptions.
- Reviews use actual summaries/timestamps and an independent own-review lookup.
  Uploaded images load through authenticated requests rather than random photos.
- See [frontend design and remaining work](docs/FRONTEND_DESIGN.md). Keep the
  Angular dependency upgrade, production release checks, owner image management
  and richer search rules as explicit follow-up work; no UI restyling is needed
  to validate the current application design.

## Further backend improvements after the current foundation

- Use asymmetric signing/workload identities if services cross independent
  trust boundaries. Current local services share signing/internal keys.
- Add a refresh/revocation policy and credential-change session invalidation
  if longer-lived sessions are required. Current access tokens expire after
  one hour and require another login.
- Move local authentication throttling to a shared edge store for multiple
  replicas; configure verified proxy identities and SMS spend limits.
- Consider object storage/CDN for uploaded images once storage volume or
  traffic warrants it. Current limits are 5 MB/image, 20 images/shop and
  200 products/shop.
- Add pending-delivery dashboards, distributed tracing and operational alerts;
  the persisted retry flags already provide the underlying state.
- Add radius/distance search with appropriate spatial indexes, opening hours,
  moderation/reporting, richer product data and approval resubmission.
- Give each service a separate least-privilege database role in deployment.
  Compose uses separate databases on one PostgreSQL instance for convenience.
- Provide a reviewed import of existing favourites/ratings into their new
  authoritative stores when migrating real historical data.
- Measure latency, database query counts and load behaviour before making
  scalability claims in a portfolio.
