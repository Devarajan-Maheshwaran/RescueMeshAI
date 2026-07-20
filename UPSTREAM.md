# Upstream tracking

RescueMesh AI is derived from the [Briar](https://github.com/briar/briar) source tree and retains its GPLv3-or-later licensing obligations.

## Imported baseline

| Field | Value |
|---|---|
| Upstream repository | `https://github.com/briar/briar.git` |
| Upstream branch | `master` |
| Imported commit | `b46d008aceb4c9cea306df8299fcfc1b7ce79d21` |
| Upstream release | 1.5.19 |
| Import date | 20 July 2026 |

The repository remote named `briar-upstream` is used only to fetch upstream changes. It is not a push target.

## RescueMesh modifications

### Phase 1 — emergency Android UI scaffold

- Added the RescueMesh dashboard and SOS/update composer fragments under `briar-android/.../rescue/`.
- Added a session-only emergency draft hand-off. It intentionally does not persist emergency text in ordinary Android SharedPreferences and does not claim any network delivery.
- Made the RescueMesh dashboard the initial navigation content and added an Emergency dashboard drawer entry.
- Rebranded the default application name string to **RescueMesh AI**.
- Added RescueMesh-specific layouts, labels, colours and status-card drawable.

### Phase 2 — structured emergency core (in progress)

- Added standalone `rescue-api` and `rescue-core` Gradle Java modules.
- Added `EmergencyEnvelopeV1`, canonical binary codec, SHA-256 content hash, strict validation, bounded seen-message cache, and regression tests.
- Added `docs/PROTOCOL_EMERGENCY_ENVELOPE_V1.md`.
- The payload is not yet wired to Briar group/forum persistence or transport. It is not public-mesh capable and does not yet authenticate non-contact senders.

## Update policy

1. Fetch upstream into `briar-upstream/master` before updating.
2. Review upstream changes affecting `bramble-*`, `briar-api`, `briar-core`, Android lifecycle, storage, crypto, transports, and dependencies.
3. Make an integration branch; do not update directly on the main development branch.
4. Resolve conflicts without removing upstream licence/copyright notices.
5. Run the applicable unit, instrumentation, and manual device tests.
6. Record upstream commit, conflicts, RescueMesh adaptations, validation result, and release impact in this document and in an ADR if architecture changes.

## Security rule

RescueMesh must not modify upstream cryptography, key exchange, or normal Briar message privacy semantics without a dedicated architecture decision, threat-model update, security review, and tests.
