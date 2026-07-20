# EmergencyEnvelopeV1 protocol note

`EmergencyEnvelopeV1` is the Phase-2 RescueMesh structured payload. It is an
internal, versioned binary payload; it is not yet a public-mesh protocol and is
not yet an emergency-service integration.

## Properties

- Schema version: `1`
- Canonical binary encoding begins with `RME1`.
- The content hash is SHA-256 over canonical envelope content **excluding** the
  hash itself.
- Maximum text size: 500 UTF-8 bytes.
- Identifiers are bounded to 128 UTF-8 bytes.
- Maximum relay hop limit: 10; maximum envelope lifetime: seven days.
- Received data is validated before it can be admitted to a queue or user
  interface.

## Fields

| Field | Purpose |
|---|---|
| schemaVersion | Decoder compatibility gate. |
| messageId | Stable random ID for idempotence/deduplication. |
| originId | Pseudonymous origin identifier; not a verified real-world identity. |
| createdAt / expiresAt | Time-bound retention and forwarding eligibility. |
| kind / priority | SOS workflow type and sender-selected urgency. |
| text / victimCount / location | Bounded, optional emergency content. Location is consent based. |
| hopCount / hopLimit | Relay bounds. |
| contentHash | Integrity identity for canonical content. |

## Authorised Briar forum bridge

For the closed pilot, the binary envelope is encoded as Base64 with the text
prefix `rescuemesh:e1:` and stored in a dedicated, already authorised Briar
forum post. The bridge deliberately uses Briar's existing encrypted forum
sharing path; it does not discover strangers, bypass group membership, or
forward ordinary Briar data.

A locally stored forum post becomes eligible for Briar synchronisation. The
current adapter does not infer remote delivery from local persistence.

## Local lifecycle

A validated envelope is tracked locally as `STORED`, `QUEUED`,
`SYNCHRONISED_TO_PEER`, `ACKNOWLEDGED`, `EXPIRED`, or `REJECTED`.
`SYNCHRONISED_TO_PEER` is set only after a future Briar adapter reports a
successful authorised peer synchronisation. Neither that state nor an ACK means
that an emergency service has been alerted.

## Security status

The content hash detects accidental/tampered changes after reception but does
**not** authenticate a sender. Admission/session authentication, signatures and
public-relay keying are intentionally deferred until the Emergency Mesh threat
model and admission design are approved. Until then, payloads must only be used
inside the existing authorised Briar pilot sharing path.
