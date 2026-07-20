package org.rescuemesh.api.emergency;

/** Offline priority-suggestion contract with a deterministic failure fallback. */
public interface PriorityClassifier {

	PrioritySuggestion classify(String text);

	/** Stable identifier for auditability and local evaluation records. */
	String getModelVersion();
}
