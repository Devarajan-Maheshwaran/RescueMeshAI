package org.briarproject.briar.android.rescue.transport;

import android.content.Context;

import org.briarproject.bramble.api.crypto.CryptoExecutor;
import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.identity.IdentityManager;
import org.briarproject.bramble.api.system.Clock;
import org.briarproject.briar.api.forum.Forum;
import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.api.emergency.EmergencyKind;
import org.rescuemesh.api.emergency.EmergencyLocation;
import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.core.emergency.EmergencyEnvelopeFactory;
import org.rescuemesh.core.emergency.EmergencyQueue;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 * Creates a validated envelope, admits it to the local lifecycle queue, and
 * stores it through an authorised Briar emergency forum.
 */
public class EmergencySubmissionController {

	private static final long CRITICAL_LIFETIME_MS = 24L * 60 * 60 * 1000;
	private static final long STANDARD_LIFETIME_MS = 12L * 60 * 60 * 1000;
	private static final int DEFAULT_HOP_LIMIT = 3;

	private final EmergencyForumRepository forumRepository;
	private final BriarEmergencyForumPublisher publisher;
	private final IdentityManager identityManager;
	private final Clock clock;
	private final EmergencyQueue queue;

	public EmergencySubmissionController(Context context,
			@DatabaseExecutor Executor databaseExecutor,
			@CryptoExecutor Executor cryptoExecutor, IdentityManager identityManager,
			org.briarproject.briar.api.forum.ForumManager forumManager, Clock clock,
			EmergencyQueue queue) {
		forumRepository = new EmergencyForumRepository(context, databaseExecutor,
				forumManager);
		publisher = new BriarEmergencyForumPublisher(databaseExecutor, cryptoExecutor,
				identityManager, forumManager, clock);
		this.identityManager = identityManager;
		this.clock = clock;
		this.queue = queue;
	}

	public void submit(EmergencyKind kind, EmergencyPriority priority, String text,
			@Nullable Integer victimCount, @Nullable EmergencyLocation location,
			SubmissionCallback callback) {
		forumRepository.getOrCreate(new EmergencyForumRepository.ForumCallback() {
			@Override
			public void onForumReady(Forum forum, boolean created) {
				try {
					long now = clock.currentTimeMillis();
					long lifetime = priority == EmergencyPriority.CRITICAL
							? CRITICAL_LIFETIME_MS : STANDARD_LIFETIME_MS;
					EmergencyEnvelope envelope = EmergencyEnvelopeFactory.create(
							identityManager.getLocalAuthor().getId().toString(), kind, priority,
							text, victimCount, location, now, now + lifetime,
							DEFAULT_HOP_LIMIT);
					EmergencyQueue.AdmissionResult admission = queue.admit(envelope, now, true);
					if (!admission.isAccepted()) {
						callback.onFailure(new IllegalStateException(admission.getReason()));
						return;
					}
					publisher.publish(forum, envelope,
							new BriarEmergencyForumPublisher.PublishCallback() {
					@Override
					public void onStored(
								org.briarproject.briar.api.forum.ForumPostHeader header) {
						queue.markQueued(envelope.getMessageId(),
								clock.currentTimeMillis());
						callback.onStored(envelope, created);
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

	public interface SubmissionCallback {
		void onStored(EmergencyEnvelope envelope, boolean forumWasCreated);
		void onFailure(Exception exception);
	}
}
