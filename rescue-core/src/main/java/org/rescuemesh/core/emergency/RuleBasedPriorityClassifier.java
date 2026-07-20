package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.api.emergency.PrioritySuggestion;
import org.rescuemesh.api.emergency.PriorityClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
					confidence(critical.size(), .80f), critical, getModelVersion());
		}
		List<String> high = matched(normalised, HIGH);
		if (!high.isEmpty()) {
			return new PrioritySuggestion(EmergencyPriority.HIGH,
					confidence(high.size(), .65f), high, getModelVersion());
		}
		return new PrioritySuggestion(EmergencyPriority.NORMAL, .40f,
				Collections.<String>emptyList(), getModelVersion());
	}

	@Override
	public String getModelVersion() {
		return "rules-en-v1";
	}

	private List<String> matched(String text, List<String> indicators) {
		List<String> matches = new ArrayList<>();
		for (String indicator : indicators) {
			// Word/phrase boundaries avoid accidental matches such as "fire" in
			// "firefighter" while retaining multi-word emergency indicators.
			Pattern pattern = Pattern.compile("(^|[^a-z])" + Pattern.quote(indicator)
					+ "(?=$|[^a-z])");
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				int indicatorStart = matcher.start() + matcher.group(1).length();
				if (!isNegatedAt(text, indicatorStart)) {
					matches.add(indicator);
					break;
				}
			}
		}
		return matches;
	}

	private boolean isNegatedAt(String text, int index) {
		int start = Math.max(0, index - 12);
		String prefix = text.substring(start, index);
		return prefix.contains("no ") || prefix.contains("not ") || prefix.contains("without ");
	}

	private float confidence(int matches, float base) {
		return Math.min(.95f, base + ((matches - 1) * .08f));
	}
}
