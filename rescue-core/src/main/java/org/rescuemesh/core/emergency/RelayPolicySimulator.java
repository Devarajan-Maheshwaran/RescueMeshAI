package org.rescuemesh.core.emergency;

import java.util.List;

/**
 * Minimal deterministic trace simulator for comparing bounded relay policies.
 * It models one message and marks it delivered when a selected candidate is
 * declared destination-reachable in a synthetic/de-identified round.
 */
public final class RelayPolicySimulator {

	private RelayPolicySimulator() {}

	public static RelaySimulationResult simulate(RelayPolicy policy,
			List<RelaySimulationRound> rounds, long expiresAt) {
		int transmissions = 0;
		int processed = 0;
		for (RelaySimulationRound round : rounds) {
			if (round.getTimestamp() >= expiresAt) break;
			processed++;
			for (RelayDecision decision : policy.selectRelays(round.getCandidates())) {
				transmissions++;
				if (round.reachesDestination(
						decision.getCandidate().getPseudonymousId())) {
					return new RelaySimulationResult(policy.getPolicyVersion(), true,
							round.getTimestamp(), transmissions, processed);
				}
			}
		}
		return new RelaySimulationResult(policy.getPolicyVersion(), false, -1L,
				transmissions, processed);
	}
}
