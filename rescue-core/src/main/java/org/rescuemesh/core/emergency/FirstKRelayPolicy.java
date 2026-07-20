package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.RelayCandidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Deterministic first-K baseline used to compare adaptive relay selection. */
public final class FirstKRelayPolicy implements RelayPolicy {

	private final int maxRelays;

	public FirstKRelayPolicy(int maxRelays) {
		if (maxRelays <= 0) throw new IllegalArgumentException("maxRelays");
		this.maxRelays = maxRelays;
	}

	@Override
	public String getPolicyVersion() { return "first-k-baseline-v1"; }

	@Override
	public List<RelayDecision> selectRelays(List<RelayCandidate> candidates) {
		List<RelayDecision> decisions = new ArrayList<>();
		for (RelayCandidate candidate : candidates) {
			if (decisions.size() == maxRelays) break;
			decisions.add(new RelayDecision(candidate, 0f));
		}
		return Collections.unmodifiableList(decisions);
	}
}
