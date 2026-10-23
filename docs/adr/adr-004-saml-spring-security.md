# ADR-004 — SAML 2.0 via Spring Security

- **Status:** Accepted
- **Date:** 2026-10-22
- **Serves:** FR-3

## Context

SAML 2.0 is required for B2B/enterprise SSO (SP-initiated and IdP-initiated).

## Decision

Add a **SAML service provider to the Java resource server** using Spring Security's
`saml2-service-provider`. Deliver **SP-initiated first**, then **IdP-initiated**
(validating audience / destination / InResponseTo on unsolicited assertions).

## Consequences

- **Pros:** first-class, mature SP support; consistent with the Java-only backend.
- **Cons:** SAML is a separate protocol + IdP config (Keycloak SAML client).

## Alternatives considered

- **`@node-saml`** — rejected: Node backend removed under C-2.
- **Hand-rolled SAML** — rejected: XML signature validation is a security risk to own.
