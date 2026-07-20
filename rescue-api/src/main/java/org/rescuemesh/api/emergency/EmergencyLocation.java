package org.rescuemesh.api.emergency;

import java.util.Locale;

/** Consent-based location captured with an emergency message. */
public final class EmergencyLocation {

	private final double latitude;
	private final double longitude;
	private final float accuracyMetres;
	private final long capturedAt;

	public EmergencyLocation(double latitude, double longitude,
			float accuracyMetres, long capturedAt) {
		if (latitude < -90d || latitude > 90d) {
			throw new IllegalArgumentException("Latitude out of range");
		}
		if (longitude < -180d || longitude > 180d) {
			throw new IllegalArgumentException("Longitude out of range");
		}
		if (Float.isNaN(accuracyMetres) || accuracyMetres < 0f) {
			throw new IllegalArgumentException("Accuracy must be non-negative");
		}
		if (capturedAt <= 0L) throw new IllegalArgumentException("Invalid capture time");
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracyMetres = accuracyMetres;
		this.capturedAt = capturedAt;
	}

	public double getLatitude() { return latitude; }
	public double getLongitude() { return longitude; }
	public float getAccuracyMetres() { return accuracyMetres; }
	public long getCapturedAt() { return capturedAt; }

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "EmergencyLocation{%.5f,%.5f}",
				latitude, longitude);
	}
}
