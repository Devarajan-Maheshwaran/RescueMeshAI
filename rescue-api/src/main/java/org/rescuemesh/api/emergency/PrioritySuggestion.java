package org.rescuemesh.api.emergency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Explainable on-device priority suggestion; it never overrides sender priority. */
public final class PrioritySuggestion {

	private final EmergencyPriority priority;
	private final float confidence;
	private final List<String> indicators;

	public PrioritySuggestion(EmergencyPriority priority, float confidence,
			List<String> indicators) {
		if (priority == null) throw new NullPointerException("priority");
		if (confidence < 0f || confidence > 1f) throw new IllegalArgumentException("confidence");
		this.priority = priority;
		this.confidence = confidence;
		this.indicators = Collections.unmodifiableList(new ArrayList<>(indicators));
	}

	public EmergencyPriority getPriority() { return priority; }
	public float getConfidence() { return confidence; }
	public List<String> getIndicators() { return indicators; }
}
