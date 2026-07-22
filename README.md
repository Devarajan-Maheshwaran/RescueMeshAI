# RescueMesh AI

> **An AI-assisted, infrastructure-less emergency communication network for Android, built by extending Briar.**

RescueMesh AI is an academic open-source project for exchanging prioritised emergency messages when cellular networks, Internet access, or normal communication infrastructure are unavailable or unreliable. It is built as a GPLv3-compatible derivative of [Briar](https://github.com/briar/briar), reusing Briar/Bramble's encrypted peer-to-peer communication and offline synchronisation foundations while adding a disaster-focused interface, structured SOS messages, local urgency prioritisation, and controlled delay-tolerant relay research.

> **Safety notice — prototype only:** RescueMesh AI is not a replacement for 112, 108, or any official emergency service. It cannot guarantee delivery, dispatch responders, provide continuous coverage, or operate when no compatible device is reachable. If an official emergency channel is available, use it first.

## Project status

**Stage:** Phase 2 trusted-pilot code complete; physical Android device validation pending
**Initial upstream baseline:** Briar `master`, commit `b46d008aceb4c9cea306df8299fcfc1b7ce79d21` (Briar 1.5.19)  
**Platform:** Android-first  
**Licence:** GPL-3.0-or-later — see [LICENSE.txt](LICENSE.txt) when the upstream source is imported.

The first implementation push contains this README and the detailed [Software Requirements Specification](SRS.md). Source import and implementation begin only after the architecture decisions in the SRS are accepted.

---

## The problem

Disasters can disrupt cellular towers, Internet backhaul, electrical power, and conventional emergency response channels. People may still have Android phones nearby, but lack a dependable way to share an SOS, a location, or a status update across intermittent local connections.

RescueMesh AI investigates whether nearby participating phones can form a **best-effort, encrypted, delay-tolerant communication layer**:

1. A person creates an SOS or emergency update.
2. The message is stored securely on the device.
3. It is exchanged over available nearby transports.
4. Participating devices retain and relay it according to bounded priority and expiry rules.
5. A rescuer/coordinator can acknowledge receipt when reachable.

This is not a promise of end-to-end rescue delivery. It is a research prototype designed to improve the chance that a time-sensitive emergency message reaches another participating device during infrastructure loss.

---

## Why Briar

Briar already provides much of the difficult base infrastructure:

- serverless, end-to-end encrypted synchronisation;
- Android Bluetooth transport;
- local Wi-Fi/LAN TCP transport;
- Tor-based communication when Internet access returns;
- secure local storage, key management, contact/group data, and delayed synchronisation;
- a mature modular Gradle codebase.

Briar's normal model is intentionally privacy-preserving and contact-based. RescueMesh AI will **not** modify or weaken normal Briar private-message behaviour. Emergency-specific capabilities will be isolated behind an explicit Emergency Mesh Mode and will operate on tightly bounded, signed emergency envelopes.

For upstream information, see [Briar](https://github.com/briar/briar) and its [public mesh research](https://code.briarproject.org/briar/public-mesh-research/-/wikis/Public-Mesh-Research-Report/diff?version_id=feee4bf861af8bc19e0e4f1531465efbdd8481a9).

---

## Planned capabilities

### Initial MVP: trusted, offline emergency communication

- Local encrypted profile and role selection: **Victim**, **Rescuer**, or **Coordinator**.
- An always-accessible SOS action.
- Structured text SOS messages with user-selected priority, timestamp, optional victim count, and optional location.
- Priority-ordered emergency feed and high-visibility critical notifications.
- Secure local persistence and store-carry-forward synchronisation between pre-enrolled/authorised RescueMesh users.
- Bluetooth and local Wi-Fi/LAN transport validation using Briar's existing support.
- Message expiry, duplicate detection, payload limits, and honest delivery/acknowledgement states.
- Offline, explainable rule-based urgency suggestion with manual override.

### Research extensions

- Quantised TensorFlow Lite classifier for on-device urgency suggestion.
- Battery-aware TTL and priority-aware forwarding policies.
- Relay-policy evaluation against fixed-TTL and blind-flooding baselines.
- Opt-in coordinator filtering and local/coarse location clustering.
- **Experimental** multi-hop emergency relay between non-contacts, with an event/community admission mechanism, strict rate limits, expiry, replay protection, and explicit user consent.

---

## What the app will not claim

- Guaranteed delivery or guaranteed response time.
- Automatic emergency-service dispatch.
- A replacement for official emergency channels.
- Universal Bluetooth or Wi-Fi compatibility across Android hardware.
- A complete live map of every node in a disaster area.
- Public relay by default or unrestricted message flooding.
- Mandatory cloud services, Firebase authentication, Google Maps, or Internet access.
- iOS support in the initial project scope.

---

## Architecture at a glance

```text
┌──────────────────────────────────────────────────────────────┐
│ RescueMesh Android application                               │
│  SOS UI · emergency feed · coordinator view · status         │
├──────────────────────────────────────────────────────────────┤
│ Rescue domain and intelligence layer                         │
│  EmergencyEnvelope · validation · deduplication · queue      │
│  rule/TFLite priority classifier · relay policy · metrics    │
├──────────────────────────────────────────────────────────────┤
│ Briar/Bramble foundation                                     │
│  encrypted storage · synchronisation · crypto · contacts     │
│  Bluetooth · LAN/local Wi-Fi TCP · optional Tor              │
└──────────────────────────────────────────────────────────────┘
```

### Technology decisions

| Area | Initial decision | Rationale |
|---|---|---|
| Client | Existing Briar Android application architecture | Minimises rewrite risk; upstream is Java, AndroidX Views/XML and Dagger. |
| Language | Java first; Kotlin only for isolated new code if justified | Matches the upstream codebase and build conventions. |
| Build | Gradle/Groovy, Java 17, Android SDK baseline inherited from Briar | Enables direct upstream integration. |
| Persistence/crypto/transports | Briar/Bramble components | Avoids rebuilding security-critical networking primitives. |
| AI | Deterministic rules first, optional TensorFlow Lite second | The app remains functional and explainable without ML. |
| Maps/cloud | Optional, never on the emergency delivery path | The project must work without Internet. |

---

## Delivery roadmap

| Phase | Outcome |
|---|---|
| **0. Foundation** | Import pinned Briar baseline, preserve GPL notices, rebrand build, CI and upstream policy. |
| **1. Emergency UX** | Local roles, dashboard, SOS composer, feed, notifications, trusted pilot group flow. |
| **2. Reliable emergency data** | Versioned envelope, expiry, deduplication, queue priority, acknowledgements, metrics. |
| **3. AI priority assist** | Explainable rules, evaluated TFLite option, sender override and offline fallback. |
| **4. DTN optimisation** | Relay policy simulations and device-drill comparison of measurable routing policies. |
| **5. Experimental mesh** | Opt-in multi-hop emergency relay research, threat model, admission and abuse protections. |
| **6. Evaluation** | Coordinator tools, accessibility, repeatable drills, findings and reproducible release. |

Detailed acceptance criteria and requirements are in [SRS.md](SRS.md). The current implementation status, gates, and ordered remaining work are in [docs/NEXT_PHASES_PLAN.md](docs/NEXT_PHASES_PLAN.md).

---

## Repository structure (target)

```text
RescueMeshAI/
├── README.md
├── SRS.md
├── LICENSE.txt                         # GPLv3 text retained from Briar
├── UPSTREAM.md                         # upstream commit and merge/update policy
├── docs/
│   ├── ADR/                            # architecture decision records
│   ├── THREAT_MODEL.md
│   ├── SAFETY.md
│   ├── PRIVACY.md
│   ├── PROTOCOL_EMERGENCY_ENVELOPE_V1.md
│   └── EVALUATION_PLAN.md
├── bramble-api/ bramble-core/ bramble-android/ bramble-java/
├── briar-api/ briar-core/
├── rescue-api/                         # emergency domain contracts
├── rescue-core/                        # policy, validation, classifier
├── rescue-android/                     # rebranded/forked app module
└── tools/                              # model, test traces and evaluation helpers
```

The upstream Briar source is not yet imported in this initial documentation-only commit.

---

## Responsible use and privacy

Emergency communication can expose sensitive details: identity, health status, approximate location, and social relationships. The project will follow these rules:

- Location sharing is optional and explicit.
- Precise location is not mandatory for creating or forwarding an SOS.
- Location is retained only as long as needed for the configured message expiry.
- The AI model runs on-device; message text is not sent to a model server.
- Battery, mobility, and peer observations are optional inputs; missing data uses conservative fallback policies.
- Normal Briar contacts, conversations, and groups must not leak into Emergency Mesh Mode.
- Relay research is opt-in, bounded, and tested for abuse before any public field trial.

---

## Contribution workflow

Until the source import is complete, contributions should focus on requirements, threat modelling, evaluation design, UX, test scenarios, and documentation.

After import:

1. Create an issue describing the problem and acceptance criteria.
2. Use a small focused branch and pull request.
3. Add or update unit/instrumentation tests.
4. Do not alter crypto, key exchange, or transport security without an ADR, review, and dedicated tests.
5. Record modifications to upstream-origin files in `UPSTREAM.md`.

Please do not submit real emergency victim information, private locations, credentials, API keys, or access tokens in issues, commits, screenshots, or training data.

---

## Build instructions

### Prerequisites

- Android Studio with **JDK 17** selected for Gradle.
- Android SDK Platform **35** and Android SDK Build-Tools **35.0.0**.
- Android device or emulator running Android 5.0/API 21 or later. Physical Android devices are required for Bluetooth/local-Wi-Fi testing.
- Git with submodule support.

### Clone and open

```bash
git clone --recurse-submodules https://github.com/Devarajan-Maheshwaran/RescueMeshAI.git
cd RescueMeshAI
```

If the repository was cloned without submodules:

```bash
git submodule update --init --recursive
```

Open the repository root in Android Studio, allow Gradle sync to complete, then select the `briar-android` run configuration. The prototype application ID is:

```text
org.rescuemesh.ai
```

### Command-line build

On macOS/Linux:

```bash
./gradlew :briar-android:assembleOfficialDebug
```

On Windows:

```bat
gradlew.bat :briar-android:assembleOfficialDebug
```

The first local build is the authoritative check for Android SDK, dependency, manifest, resource, and device compatibility. Run the two-device trusted-pilot drill only after the debug APK installs successfully.

---

## Licence and attribution

RescueMesh AI is intended to be distributed under **GNU GPL version 3 or later**, consistent with its Briar-derived codebase. The upstream Briar licence, notices, and attribution will be preserved. New RescueMesh code and documentation will use compatible licensing.

---

## Contact

Repository: <https://github.com/Devarajan-Maheshwaran/RescueMeshAI>
