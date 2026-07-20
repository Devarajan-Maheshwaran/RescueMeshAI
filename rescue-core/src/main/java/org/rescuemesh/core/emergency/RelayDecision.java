package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.RelayCandidate;

/** Explainable selection result; score is an adaptive heuristic, not ML output. */
public final class RelayDecision {

	private final RelayCandidate candidate;
	private final float score;

	public RelayDecision(RelayCandidate candidate, float score) {
		this.candidate = candidate;
		this.score = score;
	}

	public RelayCandidate getCandidate() { return candidate; }
	public float getScore() { return score; }
}
