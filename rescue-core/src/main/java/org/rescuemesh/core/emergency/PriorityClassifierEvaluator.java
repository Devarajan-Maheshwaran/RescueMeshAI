package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.LabeledPriorityExample;
import org.rescuemesh.api.emergency.PriorityClassifier;

/** Evaluates an offline classifier against de-identified labelled examples. */
public final class PriorityClassifierEvaluator {

	private PriorityClassifierEvaluator() {}

	public static PriorityEvaluationMetrics evaluate(PriorityClassifier classifier,
			Iterable<LabeledPriorityExample> examples) {
		if (classifier == null || examples == null) throw new NullPointerException();
		PriorityEvaluationMetrics metrics = new PriorityEvaluationMetrics();
		for (LabeledPriorityExample example : examples) {
			metrics.record(example.getExpectedPriority(),
					classifier.classify(example.getText()).getPriority());
		}
		return metrics;
	}
}
