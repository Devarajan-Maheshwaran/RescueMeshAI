package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.RelayCandidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Phase 4 adaptive heuristic. It limits relay fan-out and excludes peers below
 * a battery floor. It is not labelled AI unless a trained model is introduced
 * and experimentally shown to improve a stated metric.
 */
public final class BatteryAwareTopKRelayPolicy implements RelayPolicy {

	private final int maxRelays;
	private final int minimumBatteryPercent;

	public BatteryAwareTopKRelayPolicy(int maxRelays, int minimumBatteryPercent) {
		if (maxRelays <= 0) throw new IllegalArgumentException("maxRelays");
		if (minimumBatteryPercent < 0 || minimumBatteryPercent > 100) {
			throw new IllegalArgumentException("minimumBatteryPercent");
		}
		this.maxRelays = maxRelays;
		this.minimumBatteryPercent = minimumBatteryPercent;
	}

	@Override
	public String getPolicyVersion() {
		return "battery-aware-top-k-v1";
	}

	@Override
	public List<RelayDecision> selectRelays(List<RelayCandidate> candidates) {
		List<RelayDecision> eligible = new ArrayList<>();
		for (RelayCandidate candidate : candidates) {
			if (candidate.getBatteryPercent() < minimumBatteryPercent) continue;
			eligible.add(new RelayDecision(candidate, score(candidate)));
		}
		Collections.sort(eligible, new Comparator<RelayDecision>() {
			@Override
			public int compare(RelayDecision a, RelayDecision b) {
				return Float.compare(b.getScore(), a.getScore());
			}
		});
		if (eligible.size() <= maxRelays) return Collections.unmodifiableList(eligible);
		return Collections.unmodifiableList(new ArrayList<>(eligible.subList(0, maxRelays)));
	}

	private float score(RelayCandidate candidate) {
		float battery = candidate.getBatteryPercent() / 100f * .40f;
		float connectivity = Math.min(candidate.getObservedPeerCount(), 10) / 10f * .30f;
		float mobility = candidate.isMoving() ? .20f : 0f;
		float link = candidate.getLinkQuality() * .10f;
		return battery + connectivity + mobility + link;
	}
}
