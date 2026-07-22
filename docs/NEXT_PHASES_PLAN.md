# RescueMesh AI — implementation status and next-phase plan

**Status date:** 20 July 2026  
**Scope:** Current repository implementation compared with `SRS.md`  
**Principle:** Code-complete is not the same as operationally accepted. Android build, physical-device and security gates remain mandatory before field claims.

---

## 1. Current phase status

| SRS phase | Code status | Acceptance status | What remains before exit gate |
|---|---|---|---|
| **0 — Foundation & compliance** | Mostly implemented | **Open** | Clean local Android build, project CI, two-device install test. |
| **1 — Emergency UI & trusted-pilot MVP** | Implemented | **Open** | Two-device offline SOS exchange, accessibility review, visual/device QA. |
| **2 — Structured data & safe DTN** | Trusted-pilot code implemented | **Open** | Gradle/instrumentation tests and D1–D4 physical drill evidence. |
| **3 — AI priority assistance** | English rule baseline, fallback, metrics/evaluation pipeline implemented | **In progress** | Larger held-out English evaluation set; measured latency/memory/battery on Android; no TFLite decision until evidence exists. |
| **4 — Relay policy experimentation** | Policy interface, First-K baseline, battery-aware Top-K, trace simulator implemented | **In progress** | Batch trace evaluator, fixed-TTL comparator, scenario metrics and device-drill comparison. |
| **5 — Experimental Emergency Mesh Mode** | Not started by design | **Blocked/gated** | Threat model, admission design, consent UX, protocol isolation, adversarial test plan. |
| **6 — Coordinator tooling & release evaluation** | Initial coordinator overview implemented | **In progress** | Filters, optional coarse clustering, low-battery UX, accessible device QA, reproducible release and drill results. |

---

## 2. Work already completed

### Foundation, UI and trusted pilot path

- Briar 1.5.19 upstream source imported and GPLv3/upstream tracking documented.
- RescueMesh role selection, dashboard, emergency-first navigation, SOS/update composer and trusted-contact entry point.
- Emergency feed cards, lifecycle status, individual role-gated acknowledgement action and local coordinator overview.
- Optional one-time location permission/capture; SOS is still valid without location.
- `EmergencyEnvelopeV1`, content hash, canonical encoding, validation, expiry, hop bounds, queue, duplicate suppression and linked ACK support.
- Authorised Briar-forum bridge for emergency payloads, inbound event listener and configured-forum restart recovery.

### English-only priority assistance

- Explainable `rules-en-v1` classifier with CRITICAL/HIGH/NORMAL indicators.
- English word/phrase boundaries and explicit-negation handling.
- Sender-selected priority remains authoritative.
- Classifier abstraction, versioned suggestions, deterministic fallback and offline metrics/evaluator.
- Synthetic de-identified regression corpus, English annotation rubric and evaluation documentation.

### Relay experimentation foundation

- First-K comparison baseline.
- Battery-aware Top-K bounded heuristic with battery floor and configurable fan-out.
- Deterministic single-message trace simulator with expiry and transmission count.
- Policy documentation that explicitly avoids calling the heuristic AI-guided routing.

---

## 3. Recommended execution order

## Stage A — Build and acceptance readiness (complete Phase 0 prerequisites)

**Why first:** Every later Android feature depends on a real APK/device test loop. Do not add TFLite, public-mesh, or release claims until this works.

1. Install the upstream-compatible Android toolchain locally:
   - Java 17;
   - Android SDK/Build Tools required by the pinned Briar baseline;
   - Android Studio/adb and at least two test phones.
2. Run clean build and core tests from a fresh checkout.
3. Add repository CI for at minimum:
   - Gradle core unit tests;
   - debug APK compile;
   - static/lint checks where reproducible.
4. Fix any Android resource, Dagger, permission, manifest, or transport issues discovered by the real build.

**Exit evidence:** build command, tool versions, CI result, APK checksum/version, and known build limitations documented.

---

## Stage B — Trusted-pilot device drill (close Phase 1/2 operational gates)

Execute `docs/PHASE2_DEVICE_DRILL.md` unchanged before expanding network scope.

1. **D1:** Create SOS, restart app, verify trusted-forum recovery.
2. **D2:** Verify nearby authorised Briar transfer over Bluetooth; repeat over compatible local Wi-Fi/LAN when possible.
3. **D3:** Verify responder/coordinator ACK returns and transitions original SOS to `ACKNOWLEDGED`.
4. **D4:** Verify denied/unavailable location never blocks SOS.
5. **D5:** Run malformed/expired/duplicate test cases.
6. Record device model, Android version, transport, latency, duplicate behaviour, battery delta, and defects.

**Exit evidence:** reproducible two-device results and screenshots/logs with no real victim data.

---

## Stage C — Complete Phase 3 evidence, not premature TFLite

The current approved classifier remains **English-only rules baseline**.

1. Expand the synthetic/approved English evaluation corpus using the annotation rubric:
   - ambiguous but non-critical requests;
   - non-keyword phrases;
   - punctuation/case/spelling variation;
   - negation and false-alarm cases;
   - balanced CRITICAL/HIGH/NORMAL examples.
2. Preserve corpus provenance, annotator notes, class distribution and train/evaluation separation.
3. Run `tools/evaluate_priority_baseline.py` and Java `PriorityClassifierEvaluator` against the held-out set.
4. On a physical Android device, measure:
   - rule inference latency;
   - memory impact;
   - battery impact during realistic message flow.
5. Make a formal decision:
   - retain rules-only classifier if it is sufficient; or
   - begin an **English-only** TFLite candidate only if it has a realistic measurable advantage.

**Phase 3 exit evidence:** reproducible metrics, documented limitations, fallback demonstration, and proof that sender priority wins.

---

## Stage D — Complete Phase 4 experiments

1. Add batch trace input/output format for multiple `RelaySimulationRound` scenarios.
2. Add policy comparators:
   - First-K baseline;
   - fixed-TTL/fixed-fan-out baseline;
   - battery-aware Top-K heuristic.
3. Produce scenario metrics:
   - delivery ratio;
   - median and 95th-percentile delivery time;
   - transmissions/overhead;
   - expiry rate;
   - selected relay count;
   - measured device battery delta where available.
4. Use the same scenario inputs for every policy.
5. Repeat the most representative scenarios on physical devices after the trusted pilot drill works.

**Phase 4 exit evidence:** a baseline comparison report. Keep wording “adaptive heuristic” unless a trained predictor is added and demonstrably improves a pre-stated metric.

---

## Stage E — Complete Phase 6 coordinator/release preparation

1. Improve coordinator screen with local filters:
   - CRITICAL/HIGH/NORMAL;
   - lifecycle status;
   - acknowledgement state;
   - time window.
2. Add local aggregate counts and explicit status wording.
3. Add optional **coarse**, consented location clustering only after location drill validation.
4. Add low-battery relay preference UI and retention/delete controls.
5. Perform device-size, large-font, TalkBack, dark-theme and touch-target review.
6. Produce reproducible build/run instructions, limitations, GPL notices, safety statement and drill report.

**Phase 6 exit evidence:** a documented repeatable offline drill, accessibility findings, release notes and accurate supported-transport claim.

---

## 4. Phase 5 remains deliberately last

Experimental public/non-contact Emergency Mesh Mode must not start until Stages A–D have evidence. Before implementation, approve these architecture decisions:

1. Emergency-community admission: QR/event key, signed capability, or pre-enrolment.
2. Temporary discovery, pseudonymous identifiers and rotation policy.
3. Envelope authentication/signature and revocation approach.
4. Per-origin/per-peer rate limits, replay controls, TTL/hop limits and storage quotas.
5. Explicit opt-in UX, battery/privacy warning and disabling behaviour.
6. Adversarial test scenarios: flood, replay, forged CRITICAL, Sybil/tracking, non-forwarding relay and location leakage.

**Phase 5 rule:** default RescueMesh remains authorised/contact-based. Public mesh must remain experimental, opt-in, isolated from ordinary Briar data, and never be described as guaranteed emergency delivery.

---

## 5. Immediate next backlog

1. **Set up local Android build and execute Stage A.**
2. Add CI once the local build command is known to work. 
3. Run the Phase 2 trusted-pilot drill on two phones.
4. Expand the held-out English-only corpus and report Phase 3 baseline metrics.
5. Add the batch Phase 4 trace evaluator.
6. Add Phase 6 coordinator filters after the device drill confirms the feed data flow.

No new cloud backend, Flutter rewrite, iOS client, mandatory Firebase dependency, unrestricted broadcast, or public mesh protocol should be introduced in this sequence.
