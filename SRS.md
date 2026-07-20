# Software Requirements Specification (SRS)
# RescueMesh AI

| Document field | Value |
|---|---|
| Version | 0.1.0 — initial project baseline |
| Status | Draft for implementation approval |
| Date | 20 July 2026 |
| Product | RescueMesh AI |
| Repository | <https://github.com/Devarajan-Maheshwaran/RescueMeshAI> |
| Upstream foundation | Briar `master` at `b46d008aceb4c9cea306df8299fcfc1b7ce79d21` (Briar 1.5.19) |
| Licence target | GNU GPL v3 or later |
| Intended platform | Android-first |

---

## 1. Introduction

### 1.1 Purpose

This Software Requirements Specification defines the functional, non-functional, security, privacy, interface, test, and delivery requirements for RescueMesh AI. It is the authoritative baseline for creating RescueMesh AI as a disaster-oriented extension of the Briar Android codebase.

The project has two goals:

1. Deliver a credible offline emergency-messaging prototype using the transport, encryption, persistence, and synchronisation mechanisms that already exist in Briar.
2. Research and evaluate a bounded, opt-in, priority-aware delay-tolerant relay layer and on-device AI-assisted urgency classification.

This SRS intentionally distinguishes requirements that can be implemented on the existing contact-based Briar foundation from experimental public/multi-hop relay requirements that require protocol design and a separate security review.

### 1.2 Scope

RescueMesh AI will enable participating Android users to create, store, prioritise, exchange, forward, acknowledge, and inspect emergency messages during degraded connectivity. It will prioritise text SOS traffic and make location sharing optional.

The product will reuse Briar/Bramble components wherever possible. It will not replace core cryptographic mechanisms, create a mandatory cloud backend, or rewrite the entire client in Flutter or Compose during the initial phases.

### 1.3 Definitions and abbreviations

| Term | Definition |
|---|---|
| ACK | An authenticated acknowledgement that a participating RescueMesh device has received an envelope. It does **not** mean emergency services are dispatched. |
| Briar | The upstream secure peer-to-peer messaging project on which this project is based. |
| Coordinator | A RescueMesh role intended to triage and view incoming emergency traffic; it has no automatic authority outside the app. |
| DTN | Delay-Tolerant Networking: retain a message while disconnected and forward it when an eligible peer is encountered. |
| Emergency Mesh Mode | Explicitly enabled RescueMesh mode for limited exchange of emergency envelopes; separate from ordinary Briar data. |
| Envelope | A versioned, authenticated RescueMesh emergency message payload. |
| Expiry | Time after which an envelope must not be forwarded or displayed as active. |
| Hop | One successful relay transfer from one participating device to another. |
| Local Wi-Fi/LAN | A nearby TCP/IP connection using an existing Wi-Fi network or hotspot; not automatically Wi-Fi Direct. |
| MVP | Minimum Viable Product. |
| Priority | User-selected/message-suggested urgency: `CRITICAL`, `HIGH`, or `NORMAL`. |
| Relay | A participating device that carries/forwards an emergency envelope. |
| SOS | A high-urgency request for assistance created by a user. |
| TTL | Time-to-live. In RescueMesh this is enforced as hop and/or time bounds. |

### 1.4 References

- Briar source repository: <https://github.com/briar/briar>
- Briar upstream README: <https://github.com/briar/briar/blob/master/README.md>
- Briar public mesh research: <https://code.briarproject.org/briar/public-mesh-research/-/wikis/Public-Mesh-Research-Report/diff?version_id=feee4bf861af8bc19e0e4f1531465efbdd8481a9>
- RescueMesh repository: <https://github.com/Devarajan-Maheshwaran/RescueMeshAI>
- RescueMesh project overview: [README.md](README.md)

---

## 2. Overall description

### 2.1 Product perspective

RescueMesh AI is a Briar-derived Android application. It sits above the inherited Briar/Bramble foundation and introduces a constrained emergency domain layer.

```text
User interface
  Dashboard · SOS action · feed · coordinator view · status
        │
RescueMesh domain layer
  emergency envelope · validation · local state · notification
        │
Intelligence and policy layer
  priority suggestion · queue ordering · relay scoring · deduplication
        │
Briar/Bramble foundation
  secure store · protocol/sync · contacts/groups · Bluetooth · LAN · Tor
```

The MVP uses authorised/pilot contacts and a dedicated emergency sharing primitive. The experimental Emergency Mesh Mode is a subsequent, explicitly enabled extension. It must never cause normal Briar private messages, contacts, or ordinary groups to be advertised or forwarded publicly.

### 2.2 Current upstream constraints that drive requirements

The implementation plan must respect the current upstream design:

- The `briar-android` source is primarily Java with AndroidX Views/XML and Dagger dependency injection.
- Briar's application module depends on `bramble-api`, `bramble-core`, `bramble-android`, and `briar-core`.
- Existing Android transport code includes Bluetooth and LAN TCP/local Wi-Fi mechanisms. Wi-Fi Direct is not assumed to be a ready-to-use mesh transport.
- Briar's normal peer/data-sharing model is privacy-oriented and contact based; it is not a general public, multi-hop broadcast network.
- Briar is GPLv3-or-later. RescueMesh must retain compatible licensing and notices.

### 2.3 User classes

| User class | Description | Primary permissions/capabilities |
|---|---|---|
| Victim/general user | Person requesting help or sharing local status | Create SOS/update, choose consented location, view personal message status. |
| Rescuer | Participating field responder or trusted relay | Receive/acknowledge SOS, send updates, optionally relay in enabled mode. |
| Coordinator | Participant coordinating a drill/response | Filter and triage emergency feed, view aggregated/consented data, acknowledge receipt. |
| Relay node | Any enrolled device that may store/forward envelopes | Bounded forwarding governed by policy and user/device settings. |
| Research administrator | Developer/tester running a sanctioned drill | Export privacy-minimised test metrics only with participant consent. |

A role is a local UI/policy profile. It is not proof of real-world identity, responder credentials, or authority.

### 2.4 Operating environment

- Android devices supported by the selected upstream Briar baseline; initial target is Android API level 21+ subject to upstream build validation.
- Java 17 and the upstream Gradle/Android SDK toolchain for builds.
- Bluetooth and/or a compatible nearby local Wi-Fi/LAN path.
- Internet access is optional. When present, inherited Tor connectivity may be enabled according to upstream settings.
- No Google Play Services, Firebase, maps API, cloud service, or backend is required for core SOS creation, storage, or nearby relay.

### 2.5 Assumptions and dependencies

- At least two participating compatible devices must be physically/network-reachably connected for any transfer to occur.
- Background connectivity behaviour varies by Android version, device manufacturer, power policy, radio state, and user permissions.
- Location availability and precision vary. A missing location must not prevent SOS creation.
- The Briar upstream codebase is imported at the pinned version before implementation starts and is tracked through `UPSTREAM.md`.
- Any future public/non-contact mesh relies on a reviewed admission/discovery design and is not assumed to be provided by Briar.

### 2.6 Constraints

- GPLv3-or-later licence compatibility is mandatory.
- Existing Briar cryptographic invariants and normal privacy model shall not be weakened without a documented architecture decision, dedicated tests, and review.
- Emergency traffic must be bounded in size, rate, persistence, and forwarding scope to avoid denial-of-service and battery exhaustion.
- The core emergency workflow must operate without Internet connectivity.
- The product must use truthful language: it may report local/reached/acknowledged states but shall not state that emergency services have been notified unless a separately verified integration exists.

---

## 3. System modules

| Module | Responsibility | Planned implementation location |
|---|---|---|
| Rescue Android UI | Roles, dashboard, SOS action, feed, status, coordinator screens | `rescue-android` (fork/rebrand of upstream app module) |
| Rescue API | Envelope data types, priority, events, stable contracts | `rescue-api` |
| Rescue Core | Validation, serialisation, deduplication, expiry, queue/relay policy, classifier abstraction | `rescue-core` |
| Model/tooling | Corpus documentation, conversion, regression vectors, evaluation scripts | `tools/` |
| Briar/Bramble | Existing crypto, storage, sync and transport foundation | imported upstream modules |
| Documentation | Threat model, privacy policy, protocol, ADRs, evaluation plan | `docs/` |

### 3.1 Module boundary principles

1. New RescueMesh features shall be added in RescueMesh-namespaced modules/packages wherever practical.
2. Upstream code edits shall be minimal, isolated, tested, and documented in `UPSTREAM.md`.
3. The emergency protocol shall be versioned independently of the app version.
4. The app module shall not duplicate Briar's cryptography or key handling.
5. AI inference shall not be a required dependency for creating, storing, or relaying a user-selected SOS.

---

## 4. Functional requirements

Requirement priorities: **Must** = MVP/release blocker; **Should** = planned research-quality release; **Could** = gated future enhancement.

### 4.1 Account, profile, and role requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-ACC-01 | Must | The system shall create and unlock a local encrypted account using the inherited Briar account security flow. |
| FR-ACC-02 | Must | The system shall permit the user to choose a local RescueMesh role: Victim, Rescuer, or Coordinator. |
| FR-ACC-03 | Must | The system shall allow a user-editable display name without requiring cloud registration. |
| FR-ACC-04 | Must | The system shall clearly state that role selection does not verify real-world identity or emergency-service status. |
| FR-ACC-05 | Should | The system shall let a user configure low-battery forwarding participation and message-retention preferences. |

### 4.2 SOS creation requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-SOS-01 | Must | The dashboard shall expose an SOS action without navigating through a deep menu. |
| FR-SOS-02 | Must | The system shall create a text SOS with a unique message ID, creation time, expiry, selected priority, and bounded content. |
| FR-SOS-03 | Must | The SOS composer shall offer `CRITICAL`, `HIGH`, and `NORMAL` priority selection and default a dedicated SOS action to `CRITICAL`. |
| FR-SOS-04 | Must | The user shall be able to include optional victim count and optional free text. |
| FR-SOS-05 | Must | The user shall be able to share location only after explicit permission/consent; refusal or unavailability shall not block sending. |
| FR-SOS-06 | Must | The system shall display the location capture time and accuracy when location is attached. |
| FR-SOS-07 | Must | The system shall persist a valid SOS locally before attempting transmission. |
| FR-SOS-08 | Must | The system shall show a send state that does not imply external rescue dispatch. |
| FR-SOS-09 | Should | The system shall support structured SOS updates that refer to an existing SOS ID. |
| FR-SOS-10 | Could | The system may support image, audio, or file attachments only after bandwidth, privacy, and abuse limits are evaluated. |

### 4.3 Emergency envelope requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-ENV-01 | Must | The system shall serialise emergency data as a canonical, versioned `EmergencyEnvelopeV1`. |
| FR-ENV-02 | Must | An envelope shall include `schemaVersion`, `messageId`, `originId`, `createdAt`, `expiresAt`, `kind`, `priority`, bounded content, and integrity/authentication data. |
| FR-ENV-03 | Must | The supported initial kinds shall be `SOS`, `UPDATE`, `ACK`, and `STATUS`. |
| FR-ENV-04 | Must | An envelope shall contain both a maximum age and maximum-hop/forwarding bound where relay mode is used. |
| FR-ENV-05 | Must | Receivers shall reject malformed, unsupported-version, expired, over-size, unauthenticated, or invalid envelopes. |
| FR-ENV-06 | Must | Parsing and validation shall fail safely without crashing the UI/service. |
| FR-ENV-07 | Should | Envelope schema migration rules and compatibility vectors shall be documented in `docs/PROTOCOL_EMERGENCY_ENVELOPE_V1.md`. |

### 4.4 Message feed, status, and acknowledgement requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-FEED-01 | Must | The application shall display emergency envelopes in a priority-aware feed with `CRITICAL` entries visually distinguishable. |
| FR-FEED-02 | Must | Feed ordering shall prioritise CRITICAL, then HIGH, then NORMAL, with newest messages first within the same priority unless a coordinator filter specifies otherwise. |
| FR-FEED-03 | Must | The app shall display a local message lifecycle state: locally stored, eligible/queued, synchronised to a participating peer when known, acknowledged, expired, or failed validation. |
| FR-FEED-04 | Must | The wording for acknowledgement shall state that a participating device received the message, not that assistance is dispatched. |
| FR-FEED-05 | Must | A received message shall be idempotent: replayed/duplicate transfers shall not create duplicate visible cards. |
| FR-FEED-06 | Should | A coordinator may filter by priority, type, acknowledgement state, time window, and optional coarse area. |
| FR-FEED-07 | Should | The system shall send a local high-importance notification for newly received CRITICAL messages, subject to Android notification settings. |

### 4.5 Trusted offline synchronisation requirements (MVP)

| ID | Priority | Requirement |
|---|---|---|
| FR-SYNC-01 | Must | The MVP shall use Briar's existing authorised contact/group/forum synchronisation mechanisms for emergency exchange. |
| FR-SYNC-02 | Must | The MVP shall provide a documented pilot-provisioning flow for authorised RescueMesh participants. |
| FR-SYNC-03 | Must | The MVP shall reuse existing Bluetooth and compatible local Wi-Fi/LAN capabilities where available. |
| FR-SYNC-04 | Must | The system shall retain unsynchronised valid envelopes until acknowledgement, expiry, explicit deletion, or storage policy removal. |
| FR-SYNC-05 | Must | The emergency queue shall prioritise CRITICAL envelopes ahead of lower priorities while preventing indefinite starvation according to a documented fairness policy. |
| FR-SYNC-06 | Must | The app shall accurately show that no currently reachable peer is known rather than reporting a failed guarantee. |
| FR-SYNC-07 | Should | The app shall expose a privacy-minimised count of directly observed/known synchronisation peers and last successful synchronisation time. |

### 4.6 Experimental Emergency Mesh Mode requirements

These requirements are **gated** and shall not enter the default production flow until the security, privacy, and device-drill exit criteria in Section 10 are met.

| ID | Priority | Requirement |
|---|---|---|
| FR-MESH-01 | Should | The system shall provide an explicit, informed user opt-in for Emergency Mesh Mode. |
| FR-MESH-02 | Should | Emergency Mesh Mode shall exchange only validated RescueMesh emergency envelopes and required control data; it shall not expose ordinary Briar messages, contacts, group membership, or identifiers. |
| FR-MESH-03 | Should | The system shall use an emergency-community admission mechanism such as a QR/event code, signed capability, or pre-enrolled trust policy before accepting non-contact relay traffic. |
| FR-MESH-04 | Should | The system shall use ephemeral/pseudonymous identifiers where compatible with the selected security design. |
| FR-MESH-05 | Should | The system shall enforce hop limit, age limit, per-origin and per-peer rate limits, packet size limits, and deduplication before forwarding. |
| FR-MESH-06 | Should | The system shall offer a visible control to disable emergency relaying and shall show privacy/battery consequences before enabling it. |
| FR-MESH-07 | Should | The system shall not route or forward an expired, duplicate, unauthenticated, invalid, or policy-ineligible envelope. |
| FR-MESH-08 | Could | The system may include a temporary Bluetooth discovery service for eligible nearby RescueMesh peers following device compatibility testing. |
| FR-MESH-09 | Could | A Wi-Fi Direct transport may be explored only as a separately tested plugin; the MVP shall not claim it as supported solely because Android exposes Wi-Fi Direct APIs. |

### 4.7 Deduplication, expiry, and abuse-control requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-DTN-01 | Must | The system shall derive and store a stable message/content identifier sufficient to recognise a duplicate safely. |
| FR-DTN-02 | Must | The system shall maintain a bounded, persistent seen-message cache. |
| FR-DTN-03 | Must | The system shall stop forwarding an envelope when its time expiry or hop bound is reached. |
| FR-DTN-04 | Must | The system shall cap text/metadata/envelope sizes and reject oversized data before expensive processing. |
| FR-DTN-05 | Must | The system shall apply rate limits to emergency-envelope creation and relay traffic where public/experimental relay is enabled. |
| FR-DTN-06 | Must | The system shall preserve a privacy-minimised local audit state for message lifecycle and duplicate-drop statistics. |
| FR-DTN-07 | Should | The system shall use configurable storage quotas and evict expired/acknowledged lower-priority data before active CRITICAL data, subject to safety policy. |

### 4.8 AI priority-assistance requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-AI-01 | Must | The system shall provide an offline deterministic priority-suggestion baseline based on documented emergency indicators. |
| FR-AI-02 | Must | The priority component shall provide a result, confidence/strength where applicable, and human-readable explanation of detected indicators. |
| FR-AI-03 | Must | A user-selected SOS priority shall take precedence over model/rule output; automated output shall never silently downgrade it. |
| FR-AI-04 | Must | Inference failure, unavailable model assets, or unsupported language shall fall back to deterministic rules or no suggestion and shall not block messaging. |
| FR-AI-05 | Must | Text classification shall run on-device; message content shall not be sent to an ML service for the core workflow. |
| FR-AI-06 | Should | The system shall support a quantised TensorFlow Lite model only after model provenance, licence, test vectors, evaluation metrics, and model version are documented. |
| FR-AI-07 | Should | Training/evaluation material shall be de-identified, lawfully sourced, documented, and excluded from the production app unless essential. |
| FR-AI-08 | Should | The project shall report per-class precision, recall, F1/confusion matrix, model size, inference latency, and known language/domain limits. |

### 4.9 Intelligent relay-policy requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-REL-01 | Must | The initial relay implementation shall support a deterministic priority queue policy independent of ML. |
| FR-REL-02 | Should | The system shall expose a pluggable `RelayPolicy` interface for research comparison. |
| FR-REL-03 | Should | Candidate policies shall include FIFO baseline, priority queue, fixed TTL, battery-aware TTL, and a bounded top-K heuristic. |
| FR-REL-04 | Should | Battery, link quality, mobility, and observed connectivity shall be treated as optional/consented policy inputs; missing values shall trigger a conservative fallback. |
| FR-REL-05 | Should | The policy shall enforce an energy/bandwidth cap and must not allow unbounded epidemic flooding. |
| FR-REL-06 | Should | Any claim that a policy is AI-guided shall be supported by a defined predictive/learned component and comparative evaluation; otherwise it shall be called an adaptive heuristic. |

### 4.10 Location and coordinator requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-LOC-01 | Must | Location capture shall be opt-in per SOS or governed by an explicit persistent preference. |
| FR-LOC-02 | Must | The interface shall indicate when no location is attached and shall permit SOS sending in that state. |
| FR-LOC-03 | Must | The location payload shall include capture timestamp and accuracy when available. |
| FR-LOC-04 | Should | Coordinator views shall permit local/offline filtering of received, consented location data. |
| FR-LOC-05 | Should | Coordinator clustering shall use coarse/consented coordinates and operate locally; Internet map tiles shall not be mandatory. |
| FR-LOC-06 | Could | The app may add an offline base map or optional external map provider as a non-core enhancement. |

---

## 5. External interface requirements

### 5.1 User interface requirements

| ID | Requirement |
|---|---|
| UI-01 | The default dashboard shall make the SOS action visually prominent without obscuring access to message feed and status. |
| UI-02 | The SOS interaction shall use large, high-contrast touch targets and must be usable with screen readers. |
| UI-03 | The app shall use plain, truthful text for network and delivery state. |
| UI-04 | The interface shall distinguish user priority from AI-suggested priority. |
| UI-05 | The app shall display Emergency Mesh Mode as experimental when it is enabled and show its battery/privacy implications. |
| UI-06 | A mesh visualisation, if implemented, shall be labelled as direct or recently observed peers; it shall not imply complete topology knowledge. |
| UI-07 | The UI shall be usable in low-connectivity and low-battery states and shall avoid dependence on a remote API. |
| UI-08 | Accessibility validation shall include large-font, high-contrast, keyboard/switch where available, and TalkBack checks. |

### 5.2 Device and transport interfaces

| Interface | Requirement |
|---|---|
| Bluetooth | Reuse and validate the selected upstream transport; request Android permissions transparently and handle unsupported/disabled hardware. |
| Local Wi-Fi/LAN | Reuse current compatible LAN TCP/local Wi-Fi support; communicate actual readiness, not generic “Wi-Fi mesh.” |
| Wi-Fi Direct | Not a baseline interface. Any future plugin requires device-matrix tests, user-flow design, and a separate ADR. |
| Internet/Tor | Optional inherited path only; it shall not be a prerequisite for SOS creation or local storage. |
| Location | Android location APIs only after explicit user consent; unavailable location is a valid state. |
| Notifications | Android notification channels with a critical-message category; user/OS settings remain authoritative. |

### 5.3 Software interfaces

| Interface | Requirement |
|---|---|
| Briar/Bramble | RescueMesh shall consume documented/current upstream APIs and avoid duplicating protocol/crypto behaviour. |
| TensorFlow Lite | Optional model runtime, packaged offline; must have a rules fallback. |
| Persistence | Use inherited secure/local storage patterns for app data; do not introduce an unencrypted second message database without an ADR. |
| Cloud systems | No mandatory cloud interface. Any optional gateway must be disabled by default, separately documented, and outside delivery guarantees. |

---

## 6. Non-functional requirements

### 6.1 Performance and capacity

| ID | Priority | Requirement |
|---|---|---|
| NFR-PERF-01 | Must | SOS creation and local persistence shall complete without requiring network connectivity. |
| NFR-PERF-02 | Must | Envelope validation shall occur before queue admission/forwarding and shall have bounded resource usage. |
| NFR-PERF-03 | Must | Duplicate detection shall not permit unbounded growth of memory or persistent seen-state. |
| NFR-PERF-04 | Should | Priority suggestion shall complete within a defined device-tested latency budget; the target budget and test devices shall be recorded before release. |
| NFR-PERF-05 | Should | The application shall record and compare transfer latency, delivery ratio, duplicate overhead, and battery cost during repeatable drills. |
| NFR-PERF-06 | Should | Critical traffic scheduling shall be evaluated under mixed-priority queue load. |

### 6.2 Reliability and availability

| ID | Priority | Requirement |
|---|---|---|
| NFR-REL-01 | Must | The app shall preserve a valid locally created SOS across process restart until it expires, is deleted, or retention policy permits removal. |
| NFR-REL-02 | Must | The app shall recover gracefully from lost radio connectivity and resume eligible synchronisation when upstream transport permits. |
| NFR-REL-03 | Must | The app shall not report delivery as confirmed merely because a local send attempt occurred. |
| NFR-REL-04 | Must | The project documentation shall state that end-to-end delivery cannot be guaranteed. |
| NFR-REL-05 | Should | The app shall provide deterministic lifecycle transitions for queued, forwarded/peer-synchronised, acknowledged, expired, and rejected data. |

### 6.3 Security requirements

| ID | Priority | Requirement |
|---|---|---|
| NFR-SEC-01 | Must | RescueMesh shall retain GPL-compatible upstream security notices and must not replace established Briar cryptography with custom cryptography. |
| NFR-SEC-02 | Must | All accepted emergency envelopes shall be authenticated/integrity-protected according to the selected protocol design. |
| NFR-SEC-03 | Must | The system shall reject replayed, malformed, expired, oversized, and unauthorised envelopes. |
| NFR-SEC-04 | Must | Secrets, tokens, private keys, real user data, and production credentials shall never be committed to the repository or logged in plaintext. |
| NFR-SEC-05 | Must | Emergency Mesh Mode shall be logically isolated from ordinary Briar data and identity/contact disclosure. |
| NFR-SEC-06 | Should | A threat model shall be completed before enabling any non-contact relay. It shall address spoofing, replay, Sybil behaviour, flooding, tracking, location exposure, malicious relays, and device loss. |
| NFR-SEC-07 | Should | Relay controls shall limit a malicious/defective peer's ability to consume battery, storage, or airtime. |

### 6.4 Privacy requirements

| ID | Priority | Requirement |
|---|---|---|
| NFR-PRIV-01 | Must | The core workflow shall collect no cloud account or analytics identifier. |
| NFR-PRIV-02 | Must | Location, battery, mobility, and peer information shall be optional, purpose-limited, and minimised. |
| NFR-PRIV-03 | Must | Classifier inference shall run locally for the core experience. |
| NFR-PRIV-04 | Must | Logs/exports shall omit message bodies and precise location by default. |
| NFR-PRIV-05 | Should | The app shall provide local deletion controls and document data-retention behaviour. |
| NFR-PRIV-06 | Should | Research data export shall require explicit consent and use de-identification/minimisation appropriate to the study. |

### 6.5 Maintainability and portability

| ID | Priority | Requirement |
|---|---|---|
| NFR-MNT-01 | Must | New RescueMesh code shall be separated from imported upstream code whenever practical. |
| NFR-MNT-02 | Must | Every changed upstream-origin file shall be recorded in `UPSTREAM.md` with rationale. |
| NFR-MNT-03 | Must | Core envelope, validation, policy, and classifier components shall have unit tests. |
| NFR-MNT-04 | Should | Build, test, and release instructions shall be reproducible from a clean checkout. |
| NFR-MNT-05 | Should | Architecture-affecting choices shall be documented as ADRs. |

---

## 7. Data requirements

### 7.1 EmergencyEnvelopeV1 logical model

| Field | Required | Description | Validation/retention expectation |
|---|---:|---|---|
| `schemaVersion` | Yes | Protocol schema version | Must be supported. |
| `messageId` | Yes | Globally unique random identifier | Used for idempotence/replay handling. |
| `originId` | Yes | Pseudonymous protocol origin identifier | Must not be displayed as a real-world identity by default. |
| `createdAt` | Yes | Origin timestamp | Validated against reasonable bounds/policy. |
| `expiresAt` | Yes | Final forwarding/display expiry | Must be after creation and policy bounded. |
| `kind` | Yes | SOS, UPDATE, ACK, STATUS | Strict enum. |
| `priority` | Yes | CRITICAL, HIGH, NORMAL | User value retained independently of classifier. |
| `text` | Conditional | Bounded emergency description | Sanitised and size limited. |
| `victimCount` | No | Optional bounded count | Non-negative, policy-limited. |
| `location` | No | Consent-based coordinate + accuracy/time | Optional; expiry-bound. |
| `hopCount` | Relay mode | Number of relays observed | Incremented/validated by forwarding policy. |
| `hopLimit` | Relay mode | Maximum permitted hops | Policy-bounded. |
| `contentHash` | Yes | Canonical content/integrity identity | Used for duplicate/replay detection. |
| `authData` | Yes | Signature/MAC/protocol authentication data | Verified before forwarding/display as trusted. |

### 7.2 Local operational metadata

The receiving device may store local-only metadata: receipt time, validation state, queue state, attempts, duplicate count, classifier result/confidence/model version, acknowledgement relation, and user interface state. This metadata shall not be assumed trustworthy if received from another peer.

### 7.3 Retention policy

1. Expired envelopes shall not be forwarded.
2. The default retention duration shall be explicit and configurable only within safe policy limits.
3. Local users shall be able to delete stored RescueMesh data, with clear warning that deletion prevents later forwarding/inspection on that device.
4. Under storage pressure, the app shall prefer removing expired data, then acknowledged/low-priority data, before active critical envelopes, subject to a documented quota policy.

---

## 8. Safety and misuse controls

### 8.1 Safety statements

- RescueMesh AI is an academic/research communication tool, not a certified life-safety system.
- It does not guarantee a message will be delivered or seen by a responder.
- Receipt by a peer is not confirmation of rescue dispatch.
- Users should contact official emergency services where available.

### 8.2 Abuse cases to mitigate

| Abuse/failure case | Required mitigation |
|---|---|
| Flood of fabricated SOS messages | Admission controls, per-origin/per-peer rate limits, bounded queues, expiry, manual triage. |
| Replay of old SOS | Unique IDs, content hash, persistent seen cache, expiry/authentication checks. |
| Forged CRITICAL priority | Authenticated envelopes, UI indication of unverified/untrusted sources where applicable, rate limits. |
| Tracking through public discovery | Opt-in mode, ephemeral identifiers where feasible, minimal advertising, explicit privacy notice. |
| Battery depletion through relaying | Opt-in relay, battery thresholds, queue/airtime caps, low-battery mode. |
| Location exposure | Explicit consent, coarse option, expiry, no precise-location exports by default. |
| Misleading delivery claim | Strict lifecycle language and no emergency-dispatch claim. |
| Malformed packet crash | Bounded parser, validation before queue/UI, fuzz/unit tests. |

---

## 9. Verification and test requirements

### 9.1 Test levels

| Test level | Required coverage |
|---|---|
| Unit tests | Envelope serialisation/parsing, validation, expiry, duplicate cache, priority ordering, rule classifier, relay-policy fallback. |
| Integration tests | Persistence/restart recovery, message ingestion, Briar sharing primitive mapping, notifications. |
| Android instrumentation tests | SOS access, permission denial, role flow, feed ordering, accessibility labels. |
| Device interoperability tests | Bluetooth/local Wi-Fi synchronisation on documented Android device matrix. |
| DTN drill tests | 3-, 5-, and where feasible 10-device encounter scenarios with intentional disconnection/relay. |
| Security/adversarial tests | Invalid schema, tampered data, replay, duplicate storm, oversize packet, rate limit, expired/hop-limit handling. |
| ML evaluation | Held-out corpus metrics, rule baseline comparison, failure fallback, inference performance. |

### 9.2 Core acceptance scenarios

| ID | Scenario | Pass condition |
|---|---|---|
| AT-01 | Offline SOS creation | Device in airplane/no-data state creates and persists valid SOS without location permission. |
| AT-02 | Trusted nearby transfer | Two pre-enrolled devices exchange a valid SOS via an available Briar-supported nearby transport. |
| AT-03 | Restart recovery | Sender force-closes/restarts after local send; unexpired queued SOS remains available and eligible. |
| AT-04 | Duplicate delivery | Same envelope delivered 100 times results in one visible record and bounded duplicate metadata. |
| AT-05 | Expiry | Expired envelope is not forwarded and is clearly shown/handled by policy. |
| AT-06 | Priority order | CRITICAL traffic is scheduled/displayed ahead of HIGH/NORMAL while documented fairness is maintained. |
| AT-07 | Permission denial | Location/Bluetooth/notification permission denial results in clear degraded state, not crash or blocked local SOS. |
| AT-08 | Classifier failure | Missing/failed model does not block SOS; rules/no-suggestion fallback works. |
| AT-09 | Experimental replay/flood | Public-relay prototype rejects stale/replayed/over-limit traffic and preserves service for eligible messages. |
| AT-10 | Multi-hop drill | If Phase 5 is enabled, a valid SOS crosses at least two controlled relay hops within policy bounds and produces measurable logs. |

### 9.3 Evaluation metrics

The evaluation plan shall define scenario, device models, OS versions, sample count, transport state, and analysis method. At minimum report:

- delivery ratio/acknowledgement ratio by policy and scenario;
- median and 95th-percentile time to first peer receipt/acknowledgement;
- hop count distribution;
- duplicate transmissions/drops;
- queue age and expiry rate;
- battery delta attributable to the drill where measurable;
- classifier precision, recall, F1, confusion matrix, latency and model size;
- usability/accessibility findings and known device limitations.

---

## 10. Delivery plan and phase gates

### Phase 0 — Foundation and compliance

**Activities:** import pinned Briar source; preserve licence/notices; establish `UPSTREAM.md`; rename/rebrand application; make clean debug build and CI; create ADR/threat-model templates.

**Exit gate:** Two physical Android devices run the rebranded build and complete existing local setup. Build/test commands work from a clean checkout.

### Phase 1 — Emergency UI and trusted pilot MVP

**Activities:** roles, dashboard, SOS composer, priority feed, optional location, notifications, status, and pilot emergency-group/contact onboarding.

**Exit gate:** A pre-enrolled two-device offline demonstration sends/receives a persisted text SOS using an existing supported transport. UI makes no false delivery/dispatch claim.

### Phase 2 — Structured emergency data and safe DTN handling

**Activities:** `EmergencyEnvelopeV1`, validation, local read model, lifecycle state, deduplication, expiry, limits, queue priority, ACKs and metrics.

**Exit gate:** Unit/integration tests pass for malformed/duplicate/expired data; message persistence survives restart; duplicate/queue tests meet defined bounds.

### Phase 3 — AI priority assistance

**Activities:** labelled rubric/corpus governance, rules baseline, classifier abstraction, optional quantised TFLite model, evaluation and explainability.

**Exit gate:** SOS works with inference unavailable; explicit user priority wins; reproducible results demonstrate performance and documented limits.

### Phase 4 — Relay policy experimentation

**Activities:** pluggable policy, simulation/trace harness, battery-aware/fixed TTL comparisons, device-drill metrics.

**Exit gate:** Proposed policy has a baseline comparison and does not create unbounded traffic/storage/battery behaviour.

### Phase 5 — Experimental Emergency Mesh Mode

**Activities:** approved threat model; opt-in UX; temporary admission/discovery; isolated payload path; rate/hop/expiry/replay controls; adversarial/device tests.

**Exit gate:** Default mode remains private/contact-based; public mode is opt-in; tests demonstrate bounded traffic, invalid/replay rejection, and no ordinary Briar data leakage. Results are described as experimental.

### Phase 6 — Coordinator tooling and release evaluation

**Activities:** coordinator filters, optional local clustering, accessibility, low-battery mode, drill execution, reproducible release materials.

**Exit gate:** A documented offline drill is repeatable, limitations are published, and documentation accurately describes the tested capability.

---

## 11. Out of scope for the initial release

- Guaranteed end-to-end delivery, response times, or official emergency-service dispatch.
- Mandatory cloud account, Firebase Authentication, Firebase analytics, or Node.js backend.
- Full iOS client.
- A Flutter/Dart rewrite of the inherited Android application.
- Unrestricted anonymous broadcast/flooding.
- Claiming generic Wi-Fi Direct mesh support without an implemented/tested transport plugin.
- Continuous background location tracking.
- Processing/storing unconsented real emergency victim data for model training.

---

## 12. Open architecture decisions

Before Phase 2, project maintainers shall record decisions for:

1. **Emergency sharing primitive:** dedicated Briar forum versus private group for the trusted pilot.
2. **Emergency admission:** pre-enrolment, QR/event code, signed capability, or another reviewed temporary-community model.
3. **Envelope cryptographic binding:** use of inherited key/session mechanisms and method for emergency-community authentication.
4. **Default expiry/hop limits:** values must be evidence-based and configurable only within safe bounds.
5. **Priority fairness:** precise scheduling scheme preventing low-priority starvation.
6. **Location policy:** precise, coarse grid, manually selected, and retention defaults.
7. **Battery policy:** relay threshold, user overrides, and charging behaviour.
8. **AI language scope:** initial supported languages, corpus provenance, model/rule fallback.
9. **Coordinator data policy:** on-device-only triage versus any future optional gateway.

---

## 13. Requirements traceability summary

| Objective | Primary requirements |
|---|---|
| Accessible emergency SOS | FR-SOS-01 to FR-SOS-08, UI-01 to UI-08 |
| Offline secure exchange | FR-SYNC-01 to FR-SYNC-07, NFR-REL-01 to NFR-REL-05 |
| Bounded DTN behaviour | FR-ENV-01 to FR-ENV-07, FR-DTN-01 to FR-DTN-07 |
| AI novelty without unsafe dependency | FR-AI-01 to FR-AI-09, FR-REL-01 to FR-REL-06 |
| Future multi-hop research | FR-MESH-01 to FR-MESH-09, NFR-SEC-05 to NFR-SEC-07 |
| Privacy and responsible use | FR-LOC-01 to FR-LOC-06, NFR-PRIV-01 to NFR-PRIV-06 |
| Evaluation and academic evidence | Section 9, Phase 3–6 gates |

---

## 14. Change control

This SRS is versioned with the repository. A requirement change affecting protocol format, security/privacy posture, transport behaviour, licensing, data retention, or public relay must:

1. identify the impacted requirement IDs;
2. be documented in an ADR;
3. include acceptance tests and migration behaviour where applicable;
4. be reviewed before implementation; and
5. update the README and threat model if user-visible claims change.

---

## 15. Final product statement

RescueMesh AI shall be presented as an **AI-assisted, infrastructure-less, delay-tolerant emergency communication research prototype built on Briar**. Its validated contribution is to improve the prioritisation, persistence, bounded relay, and visibility of emergency information among participating Android devices during connectivity disruption. It shall not claim certainty of delivery or replace official emergency-response channels.
