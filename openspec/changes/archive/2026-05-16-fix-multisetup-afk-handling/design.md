## Context

AFK handling currently spans Paper/Folia activity detection, common AFK state management, proxy plugin messaging, and canonical time storage. In standalone Paper/Folia, the same instance detects inactivity and applies side effects. In proxy multi-setup, Paper/Folia slaves are the only instances that can observe movement, chat, interaction, and command activity, while proxy masters own canonical storage and network-wide kicking.

The current implementation relies on `afk.enabled` to load both detection and side-effect classes. That setting is intentionally staying as-is for this change. The fix should make the current configuration reliable and documented instead of introducing separate detection/enforcement settings.

## Goals / Non-Goals

**Goals:**
- Preserve the existing AFK configuration keys and command behavior.
- Ensure Bungee is treated as a proxy by platform capability checks.
- Ensure proxy masters do not run idle detection loops, but do process AFK plugin messages.
- Ensure Paper/Folia slaves can run AFK detection and send AFK state plugin messages when AFK is enabled.
- Ensure AFK reload applies updated timing and enablement state by restarting the checker as needed.
- Document the required multi-setup AFK configuration accurately.

**Non-Goals:**
- Split AFK settings into separate detection and enforcement sections.
- Change the storage schema or adjustment table format.
- Add new AFK commands or permissions.
- Redesign plugin messaging beyond the existing `loritime:afk` channel.

## Decisions

1. Keep `afk.enabled` as the single feature gate.

   `afk.enabled` will continue to mean "load AFK support on this instance." In multi-setup this means it must be enabled on Paper/Folia slaves that perform detection and on proxy masters that apply side effects. This avoids a config migration and matches the user's requested scope.

   Alternative considered: split detection and enforcement settings. This would make responsibilities explicit, but it is intentionally out of scope for this change.

2. Use platform capability checks to prevent proxy idle detection.

   `AfkStatusProvider` already checks `!server.isProxy()` before starting its repeated idle check. The immediate defect is that Bungee reports `false` for `isProxy()`. Correcting that aligns Bungee with Velocity and prevents proxy-owned idle checks.

   Alternative considered: branch directly on platform classes in AFK code. That would couple common AFK logic to platform modules and duplicate the existing `CommonServer` capability abstraction.

3. Preserve Paper/Folia slave AFK detection.

   Paper/Folia slave mode should continue to load `PaperSlavedAfkHandling`, register activity listeners, and send AFK state to the proxy over `loritime:afk` when AFK is enabled. The proxy master remains responsible for `MasteredAfkPlayerHandling` side effects.

   Alternative considered: have the proxy detect AFK independently. Proxies do not receive the full set of movement/interact events needed for the existing AFK semantics.

4. Restart the AFK checker on reload.

   Reload currently updates parsed AFK values but does not recreate the scheduled task. Reload should reload values and restart the checker so `afk.repeatCheck`, `afk.after`, and `afk.enabled` changes are reflected without requiring a full restart.

   Alternative considered: only document that restart is required. That leaves avoidable runtime inconsistency in place.

## Risks / Trade-offs

- Existing deployments may have enabled AFK only on the proxy based on current documentation -> Update docs to state that Paper/Folia slaves and the proxy master both need `afk.enabled: true` for proxy multi-setup AFK.
- Restarting the AFK checker on reload could briefly cancel and recreate the task -> Keep restart idempotent and reuse the existing `stopAfkCheck` behavior.
- Bungee behavior changes from "proxy may run local AFK checks" to "proxy only applies AFK side effects from messages" -> This aligns with Velocity and the multi-setup architecture.
