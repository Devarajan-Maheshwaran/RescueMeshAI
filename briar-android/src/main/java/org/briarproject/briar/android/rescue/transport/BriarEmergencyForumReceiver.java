package org.briarproject.briar.android.rescue.transport;

import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.core.emergency.EmergencyEnvelopeValidator;
import org.rescuemesh.core.emergency.EmergencyForumPayloadCodec;
import org.rescuemesh.core.emergency.EmergencyQueue;

import java.io.IOException;

/**
 * Safe inbound bridge for text received through an authorised Briar forum.
 * Call this only after Briar has completed its normal forum-post validation.
 */
public class BriarEmergencyForumReceiver {

	private final EmergencyQueue queue;

	public BriarEmergencyForumReceiver(EmergencyQueue queue) {
		this.queue = queue;
	}

	public ReceiveResult receive(String forumText, long now) {
		if (!EmergencyForumPayloadCodec.isEmergencyPayload(forumText)) {
			return ReceiveResult.notEmergencyPayload();
		}
		try {
			EmergencyEnvelope envelope = EmergencyForumPayloadCodec.decode(forumText);
			EmergencyQueue.AdmissionResult admission = queue.admit(envelope, now, false);
			if (admission.isAccepted()) {
				if (envelope.getKind() == org.rescuemesh.api.emergency.EmergencyKind.ACK) {
					queue.markAcknowledged(envelope.getRelatedMessageId(), now);
				}
				return ReceiveResult.accepted(envelope);
			}
			if (admission.isDuplicate()) return ReceiveResult.duplicate();
			return ReceiveResult.rejected(admission.getRejectionState(),
					admission.getReason());
		} catch (IOException e) {
			return ReceiveResult.rejected(
					EmergencyEnvelopeValidator.ValidationResult.State.INVALID,
					"Malformed emergency payload");
		}
	}

	public static final class ReceiveResult {
		public enum Type { ACCEPTED, DUPLICATE, NOT_EMERGENCY_PAYLOAD, REJECTED }
		private final Type type;
		private final EmergencyEnvelope envelope;
		private final EmergencyEnvelopeValidator.ValidationResult.State rejectionState;
		private final String reason;
		private ReceiveResult(Type type, EmergencyEnvelope envelope,
				EmergencyEnvelopeValidator.ValidationResult.State rejectionState,
				String reason) {
			this.type = type;
			this.envelope = envelope;
			this.rejectionState = rejectionState;
			this.reason = reason;
		}
		static ReceiveResult accepted(EmergencyEnvelope envelope) {
			return new ReceiveResult(Type.ACCEPTED, envelope, null, "");
		}
		static ReceiveResult duplicate() {
			return new ReceiveResult(Type.DUPLICATE, null, null, "Duplicate message");
		}
		static ReceiveResult notEmergencyPayload() {
			return new ReceiveResult(Type.NOT_EMERGENCY_PAYLOAD, null, null, "");
		}
		static ReceiveResult rejected(EmergencyEnvelopeValidator.ValidationResult.State state,
				String reason) {
			return new ReceiveResult(Type.REJECTED, null, state, reason);
		}
		public Type getType() { return type; }
		public EmergencyEnvelope getEnvelope() { return envelope; }
		public EmergencyEnvelopeValidator.ValidationResult.State getRejectionState() { return rejectionState; }
		public String getReason() { return reason; }
	}
}
