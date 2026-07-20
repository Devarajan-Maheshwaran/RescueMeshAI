package org.rescuemesh.api.emergency;

/** Consent-based, locally observed relay attributes for Phase 4 simulation. */
public final class RelayCandidate {

	private final String pseudonymousId;
	private final int batteryPercent;
	private final int observedPeerCount;
	private final boolean moving;
	private final float linkQuality;

	public RelayCandidate(String pseudonymousId, int batteryPercent,
			int observedPeerCount, boolean moving, float linkQuality) {
		if (pseudonymousId == null || pseudonymousId.length() == 0) {
			throw new IllegalArgumentException("pseudonymousId");
		}
		if (batteryPercent < 0 || batteryPercent > 100) {
			throw new IllegalArgumentException("batteryPercent");
		}
		if (observedPeerCount < 0) throw new IllegalArgumentException("observedPeerCount");
		if (linkQuality < 0f || linkQuality > 1f) {
			throw new IllegalArgumentException("linkQuality");
		}
		this.pseudonymousId = pseudonymousId;
		this.batteryPercent = batteryPercent;
		this.observedPeerCount = observedPeerCount;
		this.moving = moving;
		this.linkQuality = linkQuality;
	}

	public String getPseudonymousId() { return pseudonymousId; }
	public int getBatteryPercent() { return batteryPercent; }
	public int getObservedPeerCount() { return observedPeerCount; }
	public boolean isMoving() { return moving; }
	public float getLinkQuality() { return linkQuality; }
}
