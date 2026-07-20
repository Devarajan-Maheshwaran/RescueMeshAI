package org.rescuemesh.core.emergency;

import org.junit.Test;
import org.rescuemesh.api.emergency.RelayCandidate;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BatteryAwareTopKRelayPolicyTest {

	@Test
	public void excludesLowBatteryAndSelectsHighestScoringTopK() {
		List<RelayCandidate> candidates = Arrays.asList(
				new RelayCandidate("first", 50, 1, false, .5f),
				new RelayCandidate("low-battery", 10, 10, true, 1f),
				new RelayCandidate("best", 90, 8, true, .8f),
				new RelayCandidate("second", 80, 7, false, .9f));
		List<RelayDecision> decisions = new BatteryAwareTopKRelayPolicy(2, 20)
				.selectRelays(candidates);
		assertEquals(2, decisions.size());
		assertEquals("best", decisions.get(0).getCandidate().getPseudonymousId());
		assertEquals("second", decisions.get(1).getCandidate().getPseudonymousId());
	}
}
