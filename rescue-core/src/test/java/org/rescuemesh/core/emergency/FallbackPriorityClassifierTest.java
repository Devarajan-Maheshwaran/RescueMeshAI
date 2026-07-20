package org.rescuemesh.core.emergency;

import org.junit.Test;
import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.api.emergency.PriorityClassifier;
import org.rescuemesh.api.emergency.PrioritySuggestion;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FallbackPriorityClassifierTest {

	@Test
	public void fallsBackWhenPrimaryClassifierFails() {
		PriorityClassifier failing = new PriorityClassifier() {
			@Override
			public PrioritySuggestion classify(String text) {
				throw new IllegalStateException("model unavailable");
			}
			@Override
			public String getModelVersion() { return "failed-model"; }
		};
		FallbackPriorityClassifier classifier = new FallbackPriorityClassifier(failing,
				new RuleBasedPriorityClassifier());
		PrioritySuggestion result = classifier.classify("person trapped in fire");
		assertEquals(EmergencyPriority.CRITICAL, result.getPriority());
		assertEquals("rules-en-v1", result.getClassifierVersion());
	}

	@Test
	public void suggestionCarriesClassifierVersion() {
		PrioritySuggestion suggestion = new PrioritySuggestion(EmergencyPriority.NORMAL,
				.4f, Collections.<String>emptyList(), "test-version");
		assertEquals("test-version", suggestion.getClassifierVersion());
	}
}
