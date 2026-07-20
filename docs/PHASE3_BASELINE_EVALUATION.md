# Phase 3 baseline evaluation

## Scope

This is a reproducible **sanity evaluation**, not a claim of real-world model
accuracy. The corpus at `tools/data/synthetic_priority_eval_v1.csv` contains 24
English synthetic examples created only for initial regression coverage. It is
not FEMA data, contains no personal data, and must not be used to claim
clinical, disaster-response, non-English-language, or field performance.

## Reproduce

```bash
python3 tools/evaluate_priority_baseline.py \
  tools/data/synthetic_priority_eval_v1.csv
```

## Result: rules-en-v1

| Class | Precision | Recall | F1 |
|---|---:|---:|---:|
| CRITICAL | 1.000 | 1.000 | 1.000 |
| HIGH | 1.000 | 1.000 | 1.000 |
| NORMAL | 1.000 | 1.000 | 1.000 |

Overall accuracy: **1.000** (24/24).

The perfect result is expected because these are small seed examples using the
same documented vocabulary as the baseline rules. It demonstrates that the
implementation and evaluation script agree; it does **not** establish
real-world generalisation.

## In-app metric support

`PriorityEvaluationMetrics` provides a Java confusion-matrix implementation for
future offline evaluation tooling. It computes total examples, accuracy, and
per-class precision, recall, and F1 for `CRITICAL`, `HIGH`, and `NORMAL`.
It contains no corpus data and does not send evaluation content off-device.

## Required evidence before a TFLite decision

1. A larger held-out, independently reviewed, de-identified evaluation set.
2. Documented source licence and annotation rubric.
3. Non-keyword phrasing, spelling variation, negation, ambiguity, and false
   alarm cases.
4. Separate train/validation/test partitions for any learned model.
5. English-only scope retained unless the SRS is deliberately revised with a
   separately validated language plan.
6. Comparison against this rule baseline on precision, recall, F1, latency,
   model size, memory and battery use.

Until this evidence exists, `RuleBasedPriorityClassifier` remains the approved
Phase 3 classifier and the user-selected priority remains authoritative.
