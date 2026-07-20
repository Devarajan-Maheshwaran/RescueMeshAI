package org.rescuemesh.api.emergency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Explainable on-device priority suggestion; it never overrides sender priority. */
public final class PrioritySuggestion {

	private final EmergencyPriority priority;
	private final float confidence;
	private final List<String> indicators;
	private final String classifierVersion;

	public PrioritySuggestion(EmergencyPriority priority, float confidence,
			List<String> indicators) {
		this(priority, confidence, indicators, "unknown");
	}

	public PrioritySuggestion(EmergencyPriority priority, float confidence,
			List<String> indicators, String classifierVersion) {
		if (priority == null) throw new NullPointerException("priority");
		if (confidence < 0f || confidence > 1f) throw new IllegalArgumentException("confidence");
		if (classifierVersion == null || classifierVersion.length() == 0) {
			throw new IllegalArgumentException("classifierVersion");
		}
		this.priority = priority;
		this.confidence = confidence;
		this.indicators = Collections.unmodifiableList(new ArrayList<>(indicators));
		this.classifierVersion = classifierVersion;
	}

	public EmergencyPriority getPriority() { return priority; }
	public float getConfidence() { return confidence; }
	public List<String> getIndicators() { return indicators; }
	public String getClassifierVersion() { return classifierVersion; }
}
