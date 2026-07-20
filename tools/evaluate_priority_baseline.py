#!/usr/bin/env python3
"""Evaluate the documented English rule baseline on a labelled CSV corpus.

This script has no ML or network dependency. It intentionally mirrors
RuleBasedPriorityClassifier so a future TFLite model can be measured against
exactly the same labelled examples.
"""
import argparse
import csv
from collections import Counter

CRITICAL = ("trapped", "unconscious", "not breathing", "severe bleeding",
            "collapsed building", "drowning", "fire", "buried")
HIGH = ("injured", "need help", "medical help", "flood rising", "missing",
        "elderly", "child", "evacuate")
LABELS = ("CRITICAL", "HIGH", "NORMAL")


def negated(text, indicator):
    pos = text.find(indicator)
    prefix = text[max(0, pos - 12):pos]
    return any(marker in prefix for marker in ("no ", "not ", "without "))


def classify(text):
    text = text.lower()
    if any(word in text and not negated(text, word) for word in CRITICAL):
        return "CRITICAL"
    if any(word in text and not negated(text, word) for word in HIGH):
        return "HIGH"
    return "NORMAL"


def metric(tp, fp, fn):
    precision = tp / (tp + fp) if tp + fp else 0.0
    recall = tp / (tp + fn) if tp + fn else 0.0
    f1 = 2 * precision * recall / (precision + recall) if precision + recall else 0.0
    return precision, recall, f1


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("csv", help="CSV with expected_priority and text columns")
    args = parser.parse_args()
    pairs = []
    with open(args.csv, newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            pairs.append((row["expected_priority"], classify(row["text"])))
    confusion = Counter(pairs)
    correct = sum(expected == predicted for expected, predicted in pairs)
    print(f"Examples: {len(pairs)}")
    print(f"Accuracy: {correct / len(pairs):.3f}")
    print("\nConfusion matrix (rows=expected, columns=predicted)")
    print("expected\\predicted," + ",".join(LABELS))
    for expected in LABELS:
        print(expected + "," + ",".join(str(confusion[(expected, p)]) for p in LABELS))
    print("\nPer-class metrics")
    for label in LABELS:
        tp = confusion[(label, label)]
        fp = sum(confusion[(other, label)] for other in LABELS if other != label)
        fn = sum(confusion[(label, other)] for other in LABELS if other != label)
        p, r, f1 = metric(tp, fp, fn)
        print(f"{label}: precision={p:.3f}, recall={r:.3f}, f1={f1:.3f}")

if __name__ == "__main__":
    main()
