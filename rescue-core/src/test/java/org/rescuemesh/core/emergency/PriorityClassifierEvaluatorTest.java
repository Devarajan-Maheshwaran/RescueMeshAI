package org.rescuemesh.core.emergency;

import org.junit.Test;
import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.api.emergency.LabeledPriorityExample;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PriorityClassifierEvaluatorTest {

	@Test
	public void evaluatesEnglishExamplesThroughClassifierContract() {
		PriorityEvaluationMetrics metrics = PriorityClassifierEvaluator.evaluate(
				new RuleBasedPriorityClassifier(), Arrays.asList(
						new LabeledPriorityExample("c1", "Person trapped in fire",
								EmergencyPriority.CRITICAL),
						new LabeledPriorityExample("h1", "Injured person needs help",
								EmergencyPriority.HIGH),
						new LabeledPriorityExample("n1", "We are safe at the hall",
								EmergencyPriority.NORMAL)));
		assertEquals(3, metrics.getTotal());
		assertEquals(1f, metrics.getAccuracy(), .0001f);
	}
}
