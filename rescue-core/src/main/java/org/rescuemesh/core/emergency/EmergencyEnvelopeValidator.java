package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyEnvelope;

import java.io.IOException;
import java.security.MessageDigest;

/** Stateless admission validation before an envelope reaches UI or relay queues. */
public final class EmergencyEnvelopeValidator {

	public static final long MAX_FUTURE_SKEW_MS = 10 * 60 * 1000L;
	public static final long MAX_LIFETIME_MS = 7L * 24 * 60 * 60 * 1000;
	public static final int MAX_HOP_LIMIT = 10;
	public static final int MAX_VICTIM_COUNT = 999;

	private EmergencyEnvelopeValidator() {}

	public static ValidationResult validate(EmergencyEnvelope envelope, long now) {
		if (envelope.getSchemaVersion() != EmergencyEnvelope.SCHEMA_VERSION_1) {
			return ValidationResult.unsupported("Unsupported schema version");
		}
		if (envelope.getCreatedAt() <= 0 || envelope.getCreatedAt() > now + MAX_FUTURE_SKEW_MS) {
			return ValidationResult.invalid("Invalid creation time");
		}
		if (envelope.getExpiresAt() <= envelope.getCreatedAt()
				|| envelope.getExpiresAt() - envelope.getCreatedAt() > MAX_LIFETIME_MS) {
			return ValidationResult.invalid("Invalid expiry");
		}
		if (envelope.getExpiresAt() <= now) return ValidationResult.expired();
		if (envelope.getHopLimit() < 0 || envelope.getHopLimit() > MAX_HOP_LIMIT
				|| envelope.getHopCount() < 0 || envelope.getHopCount() > envelope.getHopLimit()) {
			return ValidationResult.invalid("Invalid hop bounds");
		}
		if (envelope.getVictimCount() != null && (envelope.getVictimCount() < 0
				|| envelope.getVictimCount() > MAX_VICTIM_COUNT)) {
			return ValidationResult.invalid("Invalid victim count");
		}
		try {
			byte[] expected = EmergencyEnvelopeCodec.sha256(
					EmergencyEnvelopeCodec.encodeContent(envelope));
			if (!MessageDigest.isEqual(expected, envelope.getContentHash())) {
				return ValidationResult.invalid("Content hash mismatch");
			}
		} catch (IOException e) {
			return ValidationResult.invalid("Unencodable envelope");
		}
		return ValidationResult.valid();
	}

	public static final class ValidationResult {
		public enum State { VALID, EXPIRED, UNSUPPORTED, INVALID }
		private final State state;
		private final String reason;

		private ValidationResult(State state, String reason) {
			this.state = state;
			this.reason = reason;
		}
		public static ValidationResult valid() { return new ValidationResult(State.VALID, ""); }
		public static ValidationResult expired() { return new ValidationResult(State.EXPIRED, "Expired"); }
		public static ValidationResult unsupported(String reason) { return new ValidationResult(State.UNSUPPORTED, reason); }
		public static ValidationResult invalid(String reason) { return new ValidationResult(State.INVALID, reason); }
		public boolean isValid() { return state == State.VALID; }
		public State getState() { return state; }
		public String getReason() { return reason; }
	}
}
