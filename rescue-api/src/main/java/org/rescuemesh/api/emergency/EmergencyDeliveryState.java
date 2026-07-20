package org.rescuemesh.api.emergency;

/** Local, truthful lifecycle state. No state means rescue-service dispatch. */
public enum EmergencyDeliveryState {
	STORED,
	QUEUED,
	SYNCHRONISED_TO_PEER,
	ACKNOWLEDGED,
	EXPIRED,
	REJECTED
}
