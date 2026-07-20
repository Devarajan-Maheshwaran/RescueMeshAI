package org.rescuemesh.core.emergency;

import org.junit.Test;
import org.rescuemesh.api.emergency.EmergencyPriority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleBasedPriorityClassifierTest {

	private final RuleBasedPriorityClassifier classifier = new RuleBasedPriorityClassifier();

	@Test
	public void classifierHasStableAuditableVersion() {
		assertEquals("rules-en-v1", classifier.getModelVersion());
	}

	@Test
	public void trappedMessageIsCriticalAndExplainable() {
		assertEquals(EmergencyPriority.CRITICAL,
				classifier.classify("Two people trapped in a collapsed building").getPriority());
		assertTrue(classifier.classify("Two people trapped").getIndicators()
				.contains("trapped"));
	}

	@Test
	public void requestForMedicalHelpIsHigh() {
		assertEquals(EmergencyPriority.HIGH,
				classifier.classify("Injured person needs medical help").getPriority());
	}

	@Test
	public void routineUpdateIsNormal() {
		assertEquals(EmergencyPriority.NORMAL,
				classifier.classify("We are safe near the school").getPriority());
	}

	@Test
	public void doesNotMatchIndicatorInsideAnotherEnglishWord() {
		assertEquals(EmergencyPriority.NORMAL, classifier.classify(
				"The firefighter station is safe and has supplies").getPriority());
	}

	@Test
	public void explicitNegationDoesNotCreateCriticalIndicator() {
		assertEquals(EmergencyPriority.NORMAL,
				classifier.classify("There is no fire at our location").getPriority());
	}
}
