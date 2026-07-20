package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyDeliveryState;
import org.rescuemesh.api.emergency.EmergencyEnvelope;

/** Local-only queue metadata paired with a validated emergency envelope. */
public final class EmergencyQueueItem {

	private final EmergencyEnvelope envelope;
	private final long receivedAt;
	private EmergencyDeliveryState state;
	private int synchronisedPeerCount;
	private long lastStateChangeAt;

	EmergencyQueueItem(EmergencyEnvelope envelope, long receivedAt,
			EmergencyDeliveryState state) {
		this.envelope = envelope;
		this.receivedAt = receivedAt;
		this.state = state;
		this.lastStateChangeAt = receivedAt;
	}

	public EmergencyEnvelope getEnvelope() { return envelope; }
	public long getReceivedAt() { return receivedAt; }
	public EmergencyDeliveryState getState() { return state; }
	public int getSynchronisedPeerCount() { return synchronisedPeerCount; }
	public long getLastStateChangeAt() { return lastStateChangeAt; }

	void transitionTo(EmergencyDeliveryState next, long at) {
		state = next;
		lastStateChangeAt = at;
	}

	void incrementSynchronisedPeerCount(long at) {
		synchronisedPeerCount++;
		transitionTo(EmergencyDeliveryState.SYNCHRONISED_TO_PEER, at);
	}
}
