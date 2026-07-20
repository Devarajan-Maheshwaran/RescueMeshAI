package org.briarproject.briar.android.rescue;

import org.rescuemesh.core.emergency.EmergencyQueue;

/** Process-scoped queue until Phase-2 database read-model persistence is added. */
public final class EmergencyRuntime {

	private static final EmergencyQueue QUEUE = new EmergencyQueue(1_000);

	private EmergencyRuntime() {}

	public static EmergencyQueue getQueue() {
		return QUEUE;
	}
}
