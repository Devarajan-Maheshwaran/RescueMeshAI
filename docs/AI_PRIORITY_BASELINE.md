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
5. Keyword rules are English-only at this stage and are not medical diagnosis.

## TFLite gate

A TensorFlow Lite model may replace or complement this baseline only after the
project has documented corpus licence/provenance, de-identification, held-out
evaluation, per-class precision/recall/F1, confusion matrix, model size,
inference latency, battery impact, and a failure fallback. The deterministic
baseline remains available if an ML model is unavailable.
