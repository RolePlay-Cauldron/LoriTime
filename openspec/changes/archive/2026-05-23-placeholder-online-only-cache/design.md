## Context

LoriTime's Paper/Folia PlaceholderAPI expansion now serves time placeholders from `PlaceholderTimeCache` and schedules storage refreshes asynchronously. That keeps render paths off database-backed storage, but PlaceholderAPI can pass offline player objects too. Offline refresh is not equivalent to online refresh in LoriTime:

- Standalone and master instances could preload recent offline player totals from database state.
- Paper slave instances do not own canonical storage and currently refresh one player at a time through plugin messaging.
- Offline players do not provide a live plugin-message carrier for slave refreshes.

Trying to support offline placeholders consistently would therefore expand the scope into bulk preloading, memory sizing, cache invalidation, and multi-setup projection behavior. The simpler runtime contract is that LoriTime time placeholders are online-player placeholders.

## Goals / Non-Goals

**Goals:**

- Make Paper/Folia time placeholders deterministic for offline player requests.
- Avoid storage queries and plugin-message refreshes for offline placeholder requests.
- Preserve current online-player cache-first behavior and asynchronous refresh.
- Document that time placeholders are online-player-only.

**Non-Goals:**

- Do not preload recent offline players into placeholder cache.
- Do not add a new config option for offline cache days.
- Do not extend the storage plugin messaging protocol with bulk cache snapshots.
- Do not change the public `LoriTimeService` API or internal storage contracts.

## Decisions

### Offline placeholder requests use fallback values without refresh

When PlaceholderAPI asks LoriTime for a time placeholder and the provided player is offline or missing, LoriTime will return the same deterministic fallback used for cache misses: numeric placeholders resolve from `0`, and formatted placeholders use the normal zero-time formatting path.

This keeps placeholder rendering synchronous and bounded. It also avoids silently starting expensive database work for players who are not part of current runtime state.

Alternative considered: preload every player seen in the last configurable number of days. That works for standalone/master database instances but creates cache sizing and startup load concerns, and it does not fit current Paper slave refresh mechanics.

### Online placeholder requests keep cache-first refresh behavior

Online players remain eligible for placeholder cache refresh. The render path checks the cache, requests an asynchronous refresh, and returns either the cached value or fallback while the refresh is pending.

This preserves the previous runtime storage threading rule while keeping live player values useful.

### Slave mode does not gain offline bulk reads

Paper slave mode will not request offline player totals from the master. The existing slave read cache remains a runtime projection for online/local consumers. Supporting offline placeholders on slaves would require a separate projection protocol with clear lifecycle and sizing rules.

## Risks / Trade-offs

- [Existing PlaceholderAPI setups may expect offline placeholders to show stored time] -> Document the online-only boundary and fallback behavior.
- [Some offline requests may return `0` even when storage has historical data] -> Keep behavior intentional and deterministic instead of performing blocking render-path reads.
- [Online state checks can vary by platform API] -> Base behavior on the `OfflinePlayer` object passed by PlaceholderAPI and add tests for offline no-refresh behavior.
