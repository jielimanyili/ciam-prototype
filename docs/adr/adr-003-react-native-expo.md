# ADR-003 — React Native client via Expo (managed)

- **Status:** Accepted
- **Date:** 2026-10-22
- **Serves:** FR-1 (mobile), FR-8 … FR-14 (mobile customer flows)

## Context

A mobile client must implement Authorization Code + PKCE and call the protected
API. A live demo needs a browser-accessible preview.

## Decision

Use **Expo (managed)** + `expo-auth-session` (built-in PKCE) + `expo-secure-store`
(refresh token storage). `expo start --web` provides the live-demo preview.

## Consequences

- **Pros:** fastest to build; web preview for the demo; less native tooling.
- **Cons:** managed Expo limits custom native modules — irrelevant for this scope.

## Alternatives considered

- **Bare React Native** — rejected: more setup, no benefit here.
- **Ionic / Capacitor** — rejected: not React Native; doesn't match the stack.
