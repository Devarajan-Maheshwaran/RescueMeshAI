package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.RelayCandidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A synthetic/de-identified contact opportunity for deterministic policy comparison. */
public final class RelaySimulationRound {

	private final long timestamp;
	private final List<RelayCandidate> candidates;
	private final Set<String> destinationReachableCandidateIds;

	public RelaySimulationRound(long timestamp, List<RelayCandidate> candidates,
			Set<String> destinationReachableCandidateIds) {
		this.timestamp = timestamp;
		this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
		this.destinationReachableCandidateIds = Collections.unmodifiableSet(
				new HashSet<>(destinationReachableCandidateIds));
	}

	public long getTimestamp() { return timestamp; }
	public List<RelayCandidate> getCandidates() { return candidates; }
	public boolean reachesDestination(String candidateId) {
		return destinationReachableCandidateIds.contains(candidateId);
	}
}
