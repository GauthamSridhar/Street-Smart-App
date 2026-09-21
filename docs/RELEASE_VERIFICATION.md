# Portfolio hardening verification - 2026-09-13

This file distinguishes local execution from deployment preparation.

## Observed checks

- Active Maven reactor: PASS after dependency/config hardening. Current reports
  contain 64 backend tests, no failures.
- Focused Shop Service verification covered fuzzy matching, opening hours,
  radius/price filters and PostgreSQL search extension migration.
- Angular 21 production build: PASS, approximately 476 kB initial bundle.
- Angular ChromeHeadless suite: PASS, 56 tests.
- npm audit: zero vulnerabilities after updating compatible dependencies and
  removing the unused webpack build-server tree. This is a dated advisory result.
- Live Compose smoke: PASS through the rebuilt Caddy lab frontend on port 8088
  for registration, approval/rejection/resubmission, product search/update,
  reviews, favourites and cross-service logout revocation.
- Least-privilege runtime: PASS. `scripts/database-roles.ps1` provisioned
  per-service database users; the roles overlay became healthy and the smoke
  flow passed using those restricted accounts.
- Runtime role check: PASS. Each app role connects only to its own database and
  has no superuser, createdb or createrole privilege.
- Compose overlays: PASS for lab and production config validation.
- Backup creation: PASS for all five domain databases. Isolated restore test:
  PASS for all five; only the temporary verification databases were dropped.
- AWS CloudFormation template: cfn-lint PASS. No AWS API deployment was performed.
- History secret scan: 41 commits, three findings (one Twilio-token occurrence,
  two Maps-key occurrences). Output was redacted. The remaining current
  `street-smart-frontend/map-api.txt` copy was removed. Provider revocation is not
  verified, and historical copies remain in Git.
- Backend dependency audit: OWASP Dependency-Check 12.2.2 completed but still
  fails at CVSS >= 7. Safe pins were applied for Tomcat, PostgreSQL JDBC,
  FreeMarker, Apache HttpClient 4/5/Core 5, Netty, Log4j and Springdoc API-only.
  Remaining hard failures are Spring Boot 3.5/Spring Framework 6.2/Spring
  Security 6.5/Spring Cloud 2025.0 advisories. Public OSS fixes require a
  Boot 4 / Spring 7 / Spring Cloud 2025.1 migration; the compatible 3.x fixes
  listed by Spring are enterprise-only. Feature-specific mitigations are enabled
  for writable actuator environment endpoints and gateway JSON-to-gRPC descriptor
  locations, but the scanner result is intentionally not marked clean.
- Recent log sweep after smoke: no ERROR lines observed. WARN lines were limited
  to Springdoc API-docs enabled, Spring `PageImpl` serialization warning and
  transient gateway load-balancer warnings during service restart.
- Twilio Verify integration: PASS for a real send to one trial-verified
  recipient. Trial accounts still reject unverified recipients, and this is not
  evidence of unrestricted production delivery.
- Automated desktop/mobile visual acceptance: PASS against the local Compose
  frontend. This validates the checked routes and viewports, not a hosted
  production deployment.

## Deployment and acceptance boundaries

Automated browser acceptance and one trial-recipient Twilio Verify send are
claimed above. Manual user-operated click-through, hosted screenshots/GIFs,
interactive Maps verification, AWS resources, domain, public TLS certificate,
collector and alert receiver are not claimed. The AWS lab is an on-demand
template with a two-hour stop timer; retained storage still has costs while
stopped.

Test counts overlap between H2 and PostgreSQL. Counts are not coverage
percentages, and no throughput or high-availability measurements are claimed.
The clean backend audit target remains a future major-framework migration, not a
small patch on the current Boot 3.5 line.
