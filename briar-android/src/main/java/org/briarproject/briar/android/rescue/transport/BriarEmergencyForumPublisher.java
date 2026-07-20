package org.briarproject.briar.android.rescue.transport;

import org.briarproject.bramble.api.crypto.CryptoExecutor;
import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.identity.IdentityManager;
import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.bramble.api.system.Clock;
import org.briarproject.briar.api.client.MessageTracker.GroupCount;
import org.briarproject.briar.api.forum.Forum;
import org.briarproject.briar.api.forum.ForumManager;
import org.briarproject.briar.api.forum.ForumPost;
import org.briarproject.briar.api.forum.ForumPostHeader;
import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.core.emergency.EmergencyForumPayloadCodec;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import static java.lang.Math.max;

/**
 * Publishes a validated RescueMesh envelope through an already authorised
 * Briar forum. This adapter does not create a public mesh or bypass Briar's
 * contact/group authorisation.
 */
public class BriarEmergencyForumPublisher {

	private final Executor dbExecutor;
	private final Executor cryptoExecutor;
	private final IdentityManager identityManager;
	private final ForumManager forumManager;
	private final Clock clock;

	public BriarEmergencyForumPublisher(@DatabaseExecutor Executor dbExecutor,
			@CryptoExecutor Executor cryptoExecutor,
			IdentityManager identityManager, ForumManager forumManager, Clock clock) {
		this.dbExecutor = dbExecutor;
		this.cryptoExecutor = cryptoExecutor;
		this.identityManager = identityManager;
		this.forumManager = forumManager;
		this.clock = clock;
	}

	/**
	 * Creates and stores a Briar forum post asynchronously.
	 *
	 * <p>{@code onStored} means the post was committed in the local Briar
	 * database and is available for Briar's normal authorised synchronisation.
	 * It does not mean a remote peer has received the message.</p>
	 */
	public void publish(Forum forum, EmergencyEnvelope envelope,
			PublishCallback callback) {
		final String payload;
		try {
			payload = EmergencyForumPayloadCodec.encode(envelope);
		} catch (IOException e) {
			callback.onFailure(e);
			return;
		}
		dbExecutor.execute(() -> preparePost(forum, payload, callback));
	}

	private void preparePost(Forum forum, String payload, PublishCallback callback) {
		try {
			LocalAuthor author = identityManager.getLocalAuthor();
			GroupCount count = forumManager.getGroupCount(forum.getId());
			long timestamp = max(count.getLatestMsgTime() + 1,
					clock.currentTimeMillis());
			cryptoExecutor.execute(() -> createPost(forum, payload, timestamp, author,
					callback));
		} catch (DbException e) {
			callback.onFailure(e);
		}
	}

	private void createPost(Forum forum, String payload, long timestamp,
			LocalAuthor author, PublishCallback callback) {
		try {
			ForumPost post = forumManager.createLocalPost(forum.getId(), payload,
					timestamp, null, author);
			dbExecutor.execute(() -> storePost(post, callback));
		} catch (RuntimeException e) {
			callback.onFailure(e);
		}
	}

	private void storePost(ForumPost post, PublishCallback callback) {
		try {
			callback.onStored(forumManager.addLocalPost(post));
		} catch (DbException e) {
			callback.onFailure(e);
		}
	}

	public interface PublishCallback {
		void onStored(ForumPostHeader header);
		void onFailure(Exception exception);
	}
}
