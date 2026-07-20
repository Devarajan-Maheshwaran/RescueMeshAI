package org.rescuemesh.core.emergency;

import org.junit.Test;
import org.rescuemesh.api.emergency.EmergencyPriority;

import static org.junit.Assert.assertEquals;

public class PriorityEvaluationMetricsTest {

	@Test
	public void calculatesConfusionMetricsPerPriority() {
		PriorityEvaluationMetrics metrics = new PriorityEvaluationMetrics();
		metrics.record(EmergencyPriority.CRITICAL, EmergencyPriority.CRITICAL);
		metrics.record(EmergencyPriority.CRITICAL, EmergencyPriority.HIGH);
		metrics.record(EmergencyPriority.HIGH, EmergencyPriority.HIGH);
		metrics.record(EmergencyPriority.NORMAL, EmergencyPriority.HIGH);

		assertEquals(4, metrics.getTotal());
		assertEquals(.5f, metrics.getAccuracy(), .0001f);
		assertEquals(1f, metrics.getPrecision(EmergencyPriority.CRITICAL), .0001f);
		assertEquals(.5f, metrics.getRecall(EmergencyPriority.CRITICAL), .0001f);
		assertEquals(2f / 3f, metrics.getF1(EmergencyPriority.CRITICAL), .0001f);
		assertEquals(2f / 3f, metrics.getPrecision(EmergencyPriority.HIGH), .0001f);
	}
}
