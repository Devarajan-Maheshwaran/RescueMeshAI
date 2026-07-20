# AI priority baseline

## Current implementation

Phase 3 begins with `RuleBasedPriorityClassifier`, a fully offline and
explainable baseline. It returns a suggested priority, confidence, and the
keywords that caused the suggestion.

| Suggested priority | Example indicators |
|---|---|
| CRITICAL | trapped, unconscious, severe bleeding, collapsed building, drowning, fire |
| HIGH | injured, need help, medical help, flood rising, missing, elderly, child |
| NORMAL | no configured urgency indicator |

## Safety rules

1. The sender's selected `EmergencyPriority` is written to the envelope.
2. A suggestion is shown to support attention and review only.
3. A classifier never silently raises or lowers the sender's selected priority.
4. Classification is local; message text is not sent to a cloud ML service.
5. Each suggestion carries an auditable classifier version.
6. Any future learned classifier must fall back to the deterministic rules classifier on inference failure.
7. Keyword rules are English-only at this stage and are not medical diagnosis.

## Reproducible seed evaluation

The synthetic, de-identified regression corpus and standard-library evaluator
are in `tools/data/synthetic_priority_eval_v1.csv` and
`tools/evaluate_priority_baseline.py`. The measured result and its limitations
are documented in [PHASE3_BASELINE_EVALUATION.md](PHASE3_BASELINE_EVALUATION.md).
This seed corpus is intentionally not evidence of field accuracy. The current
classifier and corpus are **English only**; non-English classification is out
of scope for the current SRS.

## TFLite gate

A TensorFlow Lite model may replace or complement this baseline only after the
project has documented corpus licence/provenance, de-identification, held-out
evaluation, per-class precision/recall/F1, confusion matrix, model size,
inference latency, battery impact, and a failure fallback. The deterministic
baseline remains available if an ML model is unavailable.
