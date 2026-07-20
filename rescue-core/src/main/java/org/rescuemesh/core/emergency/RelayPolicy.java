package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.RelayCandidate;

import java.util.List;

/** Bounded relay-selection contract for offline simulation and future policy work. */
public interface RelayPolicy {

	String getPolicyVersion();

	List<RelayDecision> selectRelays(List<RelayCandidate> candidates);
}
