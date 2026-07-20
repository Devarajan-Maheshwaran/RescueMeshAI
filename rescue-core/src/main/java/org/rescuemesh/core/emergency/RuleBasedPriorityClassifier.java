package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.api.emergency.PrioritySuggestion;
import org.rescuemesh.api.emergency.PriorityClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Transparent, offline Phase-3 baseline. It provides attention guidance, not
 * an authoritative decision: sender-selected urgency remains the envelope's
 * priority and is never downgraded by this classifier.
 */
public class RuleBasedPriorityClassifier implements PriorityClassifier {

	private static final List<String> CRITICAL = Collections.unmodifiableList(Arrays.asList(
			"trapped", "unconscious", "not breathing", "severe bleeding",
			"collapsed building", "drowning", "fire", "buried"));
	private static final List<String> HIGH = Collections.unmodifiableList(Arrays.asList(
			"injured", "need help", "medical help", "flood rising", "missing",
			"elderly", "child", "evacuate"));

	@Override
	public PrioritySuggestion classify(String text) {
		String normalised = text == null ? "" : text.toLowerCase(Locale.ROOT);
		List<String> critical = matched(normalised, CRITICAL);
		if (!critical.isEmpty()) {
			return new PrioritySuggestion(EmergencyPriority.CRITICAL,
					confidence(critical.size(), .80f), critical);
		}
		List<String> high = matched(normalised, HIGH);
		if (!high.isEmpty()) {
			return new PrioritySuggestion(EmergencyPriority.HIGH,
					confidence(high.size(), .65f), high);
		}
		return new PrioritySuggestion(EmergencyPriority.NORMAL, .40f,
				Collections.<String>emptyList());
	}

	@Override
	public String getModelVersion() {
		return "rules-en-v1";
	}

	private List<String> matched(String text, List<String> indicators) {
		List<String> matches = new ArrayList<>();
		for (String indicator : indicators) {
			if (text.contains(indicator) && !isNegated(text, indicator)) {
				matches.add(indicator);
			}
		}
		return matches;
	}

	private boolean isNegated(String text, String indicator) {
		int index = text.indexOf(indicator);
		if (index < 0) return false;
		int start = Math.max(0, index - 12);
		String prefix = text.substring(start, index);
		return prefix.contains("no ") || prefix.contains("not ") || prefix.contains("without ");
	}

	private float confidence(int matches, float base) {
		return Math.min(.95f, base + ((matches - 1) * .08f));
	}
}
