## 1. Placeholder Runtime Behavior

- [x] 1.1 Update Paper/Folia placeholder time rendering to detect missing or offline players before cache access.
- [x] 1.2 Return zero-time fallback values for missing or offline player requests without scheduling cache refresh.
- [x] 1.3 Preserve online-player cache lookup and asynchronous refresh behavior for canonical and slave placeholder caches.

## 2. Documentation And Tests

- [x] 2.1 Update placeholder documentation to state that time placeholders are online-player-only and offline requests return zero-time fallback values.
- [x] 2.2 Add or update Paper placeholder tests covering online refresh, offline no-refresh, and missing-player no-refresh behavior.
- [x] 2.3 Run focused Paper placeholder verification and relevant full-module checks.
