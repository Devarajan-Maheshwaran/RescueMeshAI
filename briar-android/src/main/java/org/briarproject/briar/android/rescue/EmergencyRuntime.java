package org.briarproject.briar.android.rescue;

import android.content.Context;

import org.briarproject.briar.android.AndroidComponent;
import org.briarproject.briar.android.rescue.transport.BriarEmergencyForumReceiver;
import org.briarproject.briar.android.rescue.transport.EmergencyForumEventListener;
import org.briarproject.briar.android.rescue.transport.EmergencyForumRecovery;
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
		BriarEmergencyForumReceiver receiver = new BriarEmergencyForumReceiver(QUEUE);
		EmergencyForumEventListener listener = new EmergencyForumEventListener(forums,
				receiver, component.clock(),
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
		forums.getConfigured(new EmergencyForumRepository.ConfiguredForumCallback() {
			@Override
			public void onConfiguredForum(
					@javax.annotation.Nullable org.briarproject.briar.api.forum.Forum forum) {
				if (forum == null) return;
				try {
					new EmergencyForumRecovery(component.forumManager(), receiver)
							.recover(forum, component.clock().currentTimeMillis());
				} catch (org.briarproject.bramble.api.db.DbException ignored) {
					// The live event listener remains active; recovery can retry next start.
				}
			}
			@Override
			public void onFailure(Exception exception) {
				// A configured forum is optional; do not prevent normal Briar startup.
			}
		});
		started = true;
	}
}
