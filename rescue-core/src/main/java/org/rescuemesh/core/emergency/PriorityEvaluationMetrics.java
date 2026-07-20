package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyPriority;

import java.util.EnumMap;

/**
 * Deterministic in-memory confusion-matrix metrics for evaluating any
 * English-only priority classifier against a labelled held-out corpus.
 */
public class PriorityEvaluationMetrics {

	private final EnumMap<EmergencyPriority, EnumMap<EmergencyPriority, Integer>>
			confusion = new EnumMap<>(EmergencyPriority.class);
	private int total;
	private int correct;

	public PriorityEvaluationMetrics() {
		for (EmergencyPriority expected : EmergencyPriority.values()) {
			EnumMap<EmergencyPriority, Integer> row =
					new EnumMap<>(EmergencyPriority.class);
			for (EmergencyPriority predicted : EmergencyPriority.values()) {
				row.put(predicted, 0);
			}
			confusion.put(expected, row);
		}
	}

	public void record(EmergencyPriority expected, EmergencyPriority predicted) {
		confusion.get(expected).put(predicted,
				confusion.get(expected).get(predicted) + 1);
		total++;
		if (expected == predicted) correct++;
	}

	public int getCount(EmergencyPriority expected, EmergencyPriority predicted) {
		return confusion.get(expected).get(predicted);
	}

	public int getTotal() { return total; }

	public float getAccuracy() {
		return total == 0 ? 0f : (float) correct / total;
	}

	public float getPrecision(EmergencyPriority label) {
		int tp = getCount(label, label);
		int predicted = 0;
		for (EmergencyPriority expected : EmergencyPriority.values()) {
			predicted += getCount(expected, label);
		}
		return predicted == 0 ? 0f : (float) tp / predicted;
	}

	public float getRecall(EmergencyPriority label) {
		int tp = getCount(label, label);
		int expected = 0;
		for (EmergencyPriority predicted : EmergencyPriority.values()) {
			expected += getCount(label, predicted);
		}
		return expected == 0 ? 0f : (float) tp / expected;
	}

	public float getF1(EmergencyPriority label) {
		float precision = getPrecision(label);
		float recall = getRecall(label);
		return precision + recall == 0f ? 0f
				: 2f * precision * recall / (precision + recall);
	}
}
