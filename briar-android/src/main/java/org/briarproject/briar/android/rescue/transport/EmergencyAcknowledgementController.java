package org.briarproject.briar.android.rescue.transport;

import android.content.Context;

import org.briarproject.bramble.api.crypto.CryptoExecutor;
import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.identity.IdentityManager;
import org.briarproject.bramble.api.system.Clock;
import org.briarproject.briar.api.forum.Forum;
import org.briarproject.briar.api.forum.ForumManager;
import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.core.emergency.EmergencyEnvelopeFactory;
import org.rescuemesh.core.emergency.EmergencyQueue;

import java.util.concurrent.Executor;

/** Creates and publishes a linked ACK through the configured trusted forum. */
public class EmergencyAcknowledgementController {

	private static final long ACK_LIFETIME_MS = 12L * 60 * 60 * 1000;

	private final EmergencyForumRepository forums;
	private final BriarEmergencyForumPublisher publisher;
	private final IdentityManager identityManager;
	private final Clock clock;
	private final EmergencyQueue queue;

	public EmergencyAcknowledgementController(Context context,
			@DatabaseExecutor Executor databaseExecutor,
			@CryptoExecutor Executor cryptoExecutor, IdentityManager identityManager,
			ForumManager forumManager, Clock clock, EmergencyQueue queue) {
		forums = new EmergencyForumRepository(context, databaseExecutor, forumManager);
		publisher = new BriarEmergencyForumPublisher(databaseExecutor, cryptoExecutor,
				identityManager, forumManager, clock);
		this.identityManager = identityManager;
		this.clock = clock;
		this.queue = queue;
	}

	public void acknowledge(String relatedMessageId, Callback callback) {
		forums.getOrCreate(new EmergencyForumRepository.ForumCallback() {
			@Override
			public void onForumReady(Forum forum, boolean created) {
				try {
					long now = clock.currentTimeMillis();
					EmergencyEnvelope ack = EmergencyEnvelopeFactory.createAcknowledgement(
							identityManager.getLocalAuthor().getId().toString(),
							relatedMessageId, now, now + ACK_LIFETIME_MS);
					EmergencyQueue.AdmissionResult admission = queue.admit(ack, now, true);
					if (!admission.isAccepted()) {
						callback.onFailure(new IllegalStateException(admission.getReason()));
						return;
					}
					publisher.publish(forum, ack,
							new BriarEmergencyForumPublisher.PublishCallback() {
						@Override
						public void onStored(
									org.briarproject.briar.api.forum.ForumPostHeader header) {
							queue.markQueued(ack.getMessageId(), clock.currentTimeMillis());
							callback.onAcknowledgementQueued(ack);
						}
						@Override
						public void onFailure(Exception exception) {
							callback.onFailure(exception);
						}
					});
				} catch (DbException e) {
					callback.onFailure(e);
				}
			}
			@Override
			public void onFailure(Exception exception) {
				callback.onFailure(exception);
			}
		});
	}

	public interface Callback {
		void onAcknowledgementQueued(EmergencyEnvelope acknowledgement);
		void onFailure(Exception exception);
	}
}
