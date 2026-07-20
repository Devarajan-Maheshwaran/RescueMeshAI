package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.PriorityClassifier;
import org.rescuemesh.api.emergency.PrioritySuggestion;

/**
 * Ensures classifier failure never blocks SOS composition. A future English
 * TFLite classifier can be the primary implementation; this deterministic
 * rules classifier remains the mandatory fallback.
 */
public final class FallbackPriorityClassifier implements PriorityClassifier {

	private final PriorityClassifier primary;
	private final PriorityClassifier fallback;

	public FallbackPriorityClassifier(PriorityClassifier primary,
			PriorityClassifier fallback) {
		if (primary == null || fallback == null) throw new NullPointerException();
		this.primary = primary;
		this.fallback = fallback;
	}

	@Override
	public PrioritySuggestion classify(String text) {
		try {
			return primary.classify(text);
		} catch (RuntimeException e) {
			return fallback.classify(text);
		}
	}

	@Override
	public String getModelVersion() {
		return primary.getModelVersion() + "|fallback=" + fallback.getModelVersion();
	}
}
