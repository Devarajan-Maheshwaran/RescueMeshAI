# Phase 3 English priority annotation rubric

## Scope

This rubric governs the English-only RescueMesh priority classifier baseline
and any future English-only evaluation corpus. Non-English classification is
out of scope for the current SRS and implementation.

## Unit of annotation

One short user-authored emergency message receives one label. Annotators must
use message content only; they must not infer facts from a sender's role,
location, history, or identity.

## Labels

| Label | Assign when the text explicitly indicates |
|---|---|
| `CRITICAL` | Immediate threat to life or entrapment: trapped/buried, unconscious/not breathing, severe bleeding, active fire affecting people, drowning, or comparable immediate danger. |
| `HIGH` | Urgent assistance is needed but the message does not explicitly establish a critical indicator: injury, medical help, missing child/person, rising flood, evacuation, vulnerable person needing help. |
| `NORMAL` | Status, logistics, supplies, route information, safety confirmation, or coordination without an explicit urgent indicator. |

## Rules

1. Use the highest supported urgency explicitly present in the message.
2. Do not label a message `CRITICAL` only because it contains an emotional
   phrase such as “urgent” or “please respond.”
3. Respect explicit negation where unambiguous: “no injuries” and “no fire” are
   not positive injury/fire indicators.
4. If wording is ambiguous, label conservatively and record the ambiguity in
   the annotation notes; do not invent medical facts.
5. Personal data, real names, phone numbers, exact addresses, and precise GPS
   coordinates must be removed before examples are placed in a development or
   evaluation corpus.
6. The sender-selected priority remains authoritative in the application even
   if an annotator or classifier label differs.

## Review process for a future corpus

- Two independent English-proficient annotators label each item.
- Disagreements are resolved by a documented adjudication pass.
- Report agreement rate and class distribution.
- Keep evaluation examples separate from any training examples used by a future
  learned model.
- Preserve source licence/provenance for every non-synthetic item.
