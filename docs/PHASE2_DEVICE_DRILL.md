# Phase 2 trusted-offline device drill

## Purpose

This procedure validates the Phase 2 RescueMesh path over Briar's existing,
authorised nearby synchronisation. It is not a public-mesh test and does not
make emergency-service delivery claims.

## Required devices and preparation

- Two Android devices in the Briar-supported API/device range.
- Same RescueMesh build installed on both devices.
- Two separate local Briar accounts created and unlocked.
- Devices added as trusted Briar contacts before the drill.
- A RescueMesh Emergency Pilot forum created on Device A and shared to Device B
  using Briar's normal forum-sharing workflow.
- Bluetooth enabled on both devices. Optionally repeat using a common local
  Wi-Fi/hotspot path.
- No cellular data or Internet required for the core scenario.

## Test scenarios

### D1 — Local SOS persistence and queueing

1. On Device A choose Victim role.
2. Create a CRITICAL SOS with text and optional victim count.
3. Confirm the app reports local storage/queueing, not delivery.
4. Restart the app and return to the RescueMesh feed.

**Pass:** the SOS is reconstructed from the configured emergency forum and is
shown in the priority feed.

### D2 — Nearby trusted delivery

1. Keep Device A and B nearby with an enabled supported transport.
2. Wait for normal Briar synchronisation.
3. Open the RescueMesh feed on Device B.

**Pass:** Device B displays one valid CRITICAL SOS card. Repeated sync must not
create duplicate cards.

### D3 — Acknowledgement

1. On Device B choose Rescuer or Coordinator role.
2. Use the SOS card's acknowledgement action.
3. Wait for normal Briar synchronisation back to Device A.
4. Open Device A's feed.

**Pass:** Device A's linked SOS becomes `ACKNOWLEDGED`. The interface must not
state that official services were dispatched.

### D4 — Location denial and unavailable location

1. Deny location permission on Device A, or disable location providers.
2. Select optional location and create an SOS.

**Pass:** RescueMesh shows that location was not attached and still queues the
SOS successfully.

### D5 — Expiry and malformed data handling

1. Use unit tests for expiry/hash validation.
2. For a controlled code test, present malformed/non-RescueMesh forum text to
the receiver.

**Pass:** expired or malformed data is not admitted as an SOS and does not crash
the application.

## Metrics to record

For each scenario, record device model, Android version, transport, start/end
times, queue time, first receipt time, ACK time, duplicate count, and observed
battery delta. Do not include real victim text or precise coordinates in public
logs.

## Completion rule

Phase 2 is accepted only after D1–D4 are repeated on at least two physical
Android devices and results/known transport limitations are recorded. No APK
build or physical drill has been run from this development environment.
