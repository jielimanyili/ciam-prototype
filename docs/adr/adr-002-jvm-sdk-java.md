# ADR-002 — JVM SDK in Java (Kotlin-interop)

- **Status:** Accepted
- **Date:** 2026-10-22
- **Serves:** FR-6

## Context

The reusable identity SDK needs a language. C-2 makes Java the sole backend
language; Kotlin is attractive (null-safety, sealed types) but is not the
author's production depth.

## Decision

Implement the SDK in **Java**, designed for **Kotlin interop** (Java classes are
Kotlin-callable with zero extra work). Add a thin **Kotlin DSL wrapper** later,
only where null-safety / sealed types add value.

## Consequences

- **Pros:** matches C-2 and the reference stack; Java is the production depth;
  interop gives Kotlin-first teams a first-class experience.
- **Cons:** loses Kotlin's built-in null-safety / immutability in the core.

## Alternatives considered

- **Kotlin-first** — rejected: over-claims depth; the stack targets Java.
- **Dual Java + Kotlin now** — rejected: wrapper maintenance overhead before it earns it.
