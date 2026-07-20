package org.rescuemesh.core.emergency;

import org.junit.Test;
import org.rescuemesh.api.emergency.RelayCandidate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RelayPolicySimulatorTest {

	@Test
	public void comparesPoliciesOnSameTrace() {
		RelayCandidate first = new RelayCandidate("first", 30, 0, false, .2f);
		RelayCandidate best = new RelayCandidate("best", 90, 9, true, .9f);
		RelaySimulationRound round = new RelaySimulationRound(100L,
				Arrays.asList(first, best), new HashSet<>(Collections.singletonList("best")));

		RelaySimulationResult baseline = RelayPolicySimulator.simulate(
				new FirstKRelayPolicy(1), Collections.singletonList(round), 200L);
		RelaySimulationResult adaptive = RelayPolicySimulator.simulate(
				new BatteryAwareTopKRelayPolicy(1, 20),
				Collections.singletonList(round), 200L);

		assertFalse(baseline.isDelivered());
		assertTrue(adaptive.isDelivered());
		assertEquals(100L, adaptive.getDeliveredAt());
		assertEquals(1, adaptive.getTransmissions());
	}

	@Test
	public void doesNotProcessRoundsAtOrAfterExpiry() {
		RelayCandidate candidate = new RelayCandidate("peer", 90, 8, true, .9f);
		RelaySimulationRound round = new RelaySimulationRound(200L,
				Collections.singletonList(candidate),
				new HashSet<>(Collections.singletonList("peer")));
		RelaySimulationResult result = RelayPolicySimulator.simulate(
				new FirstKRelayPolicy(1), Collections.singletonList(round), 200L);
		assertFalse(result.isDelivered());
		assertEquals(0, result.getRoundsProcessed());
	}
}
