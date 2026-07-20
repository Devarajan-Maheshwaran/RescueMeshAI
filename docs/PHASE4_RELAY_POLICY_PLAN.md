# Phase 4 relay-policy plan

## Implemented policy components

| Policy | Version | Purpose |
|---|---|---|
| First-K baseline | `first-k-baseline-v1` | Selects the first eligible K candidates without scoring. |
| Battery-aware Top-K | `battery-aware-top-k-v1` | Selects at most K candidates using locally observed battery, peer count, movement and link-quality inputs. Excludes candidates below a configured battery floor. |

The battery-aware policy is an **adaptive heuristic**, not an AI claim. It may
only be called AI-guided if a trained/predictive policy is later implemented
and demonstrates improvement against a stated baseline.

## Score

```text
battery       = batteryPercent / 100 × 0.40
connectivity  = min(observedPeerCount, 10) / 10 × 0.30
mobility      = moving ? 0.20 : 0.00
link quality  = linkQuality × 0.10
score         = battery + connectivity + mobility + link quality
```

## Privacy and safety constraints

- Inputs are locally observed and optional; no relay telemetry is uploaded.
- Missing/unknown telemetry must use a conservative baseline policy.
- Low-battery peers are excluded using the configured battery floor.
- Selection is bounded to top-K; it cannot create unlimited epidemic flooding.
- This policy is simulation/research code until device-drill evidence exists.

## Required evaluation

Compare the same controlled trace/scenario using First-K and Battery-aware
Top-K. Record delivery ratio, median/95th-percentile latency, duplicate
transmissions, selected relay count, expiry rate, and battery delta. Use
consented synthetic or de-identified contact traces only.
