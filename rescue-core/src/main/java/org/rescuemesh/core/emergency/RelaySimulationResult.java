package org.rescuemesh.core.emergency;

/** Aggregate result from one message trace under one bounded relay policy. */
public final class RelaySimulationResult {

	private final String policyVersion;
	private final boolean delivered;
	private final long deliveredAt;
	private final int transmissions;
	private final int roundsProcessed;

	RelaySimulationResult(String policyVersion, boolean delivered, long deliveredAt,
			int transmissions, int roundsProcessed) {
		this.policyVersion = policyVersion;
		this.delivered = delivered;
		this.deliveredAt = deliveredAt;
		this.transmissions = transmissions;
		this.roundsProcessed = roundsProcessed;
	}

	public String getPolicyVersion() { return policyVersion; }
	public boolean isDelivered() { return delivered; }
	public long getDeliveredAt() { return deliveredAt; }
	public int getTransmissions() { return transmissions; }
	public int getRoundsProcessed() { return roundsProcessed; }
}
