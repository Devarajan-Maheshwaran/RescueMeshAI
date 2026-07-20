# Phase 2 completion status

## Implemented code scope

- `EmergencyEnvelopeV1` with schema version, identifiers, expiry, priority,
  optional victim count/location, hop bounds, linked ACK ID and content hash.
- Canonical encoding, safe decoding, validation, duplicate suppression, expiry
  handling and priority queueing.
- Trusted Briar forum provision/recovery/publish/receive bridge.
- Local SOS and update queueing through the existing authorised Briar forum
  path.
- Inbound event handling, restart recovery and priority feed cards.
- Linked ACK creation and responder/coordinator UI action.
- Optional one-time permissioned location capture; no background tracking.
- Pure-Java core tests and the offline device-drill procedure.

## Explicitly deferred

- Public/non-contact emergency mesh; that is a later gated research phase.
- Actual peer-sync count reporting: Briar's current forum integration does not
  expose per-envelope peer delivery receipts through this adapter.
- TFLite model: Phase 3 currently uses the documented deterministic baseline.
- Formal Android instrumentation run and physical device drill: these require
  the Android SDK/device environment and must be performed before acceptance.

## Acceptance evidence required

See [PHASE2_DEVICE_DRILL.md](PHASE2_DEVICE_DRILL.md). The phase is code-complete
for the trusted-pilot scope, but operational acceptance remains pending the
physical device drill.
