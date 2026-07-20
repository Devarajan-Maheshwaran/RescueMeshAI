package org.briarproject.briar.android.rescue;

import android.content.Context;

import org.briarproject.briar.android.AndroidComponent;
import org.briarproject.briar.android.rescue.transport.BriarEmergencyForumReceiver;
import org.briarproject.briar.android.rescue.transport.EmergencyForumEventListener;
import org.briarproject.briar.android.rescue.transport.EmergencyForumRepository;
import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.core.emergency.EmergencyQueue;

/** Process-scoped queue and one-time trusted-forum inbound event registration. */
public final class EmergencyRuntime {

	private static final EmergencyQueue QUEUE = new EmergencyQueue(1_000);
	private static boolean started;

	private EmergencyRuntime() {}

	public static EmergencyQueue getQueue() {
		return QUEUE;
	}

	public static synchronized void start(Context context, AndroidComponent component) {
		if (started) return;
		EmergencyForumRepository forums = new EmergencyForumRepository(context,
				component.databaseExecutor(), component.forumManager());
		EmergencyForumEventListener listener = new EmergencyForumEventListener(forums,
				new BriarEmergencyForumReceiver(QUEUE), component.clock(),
				new EmergencyForumEventListener.InboundListener() {
				@Override
				public void onEmergencyAccepted(EmergencyEnvelope envelope) {
					// Queue admission already occurred in BriarEmergencyForumReceiver.
				}
				@Override
				public void onEmergencyRejected(String reason) {
					// Invalid remote data is deliberately not surfaced as an SOS.
				}
				});
		component.eventBus().addListener(listener);
		started = true;
	}
}
