package org.briarproject.briar.android.rescue.transport;

import org.briarproject.bramble.api.db.DbException;
import org.briarproject.briar.api.forum.Forum;
import org.briarproject.briar.api.forum.ForumManager;
import org.briarproject.briar.api.forum.ForumPostHeader;

import java.util.Collection;

/** Rebuilds the in-memory emergency feed from the configured Briar forum. */
public class EmergencyForumRecovery {

	private final ForumManager forumManager;
	private final BriarEmergencyForumReceiver receiver;

	public EmergencyForumRecovery(ForumManager forumManager,
			BriarEmergencyForumReceiver receiver) {
		this.forumManager = forumManager;
		this.receiver = receiver;
	}

	/** Must be called from Briar's database executor. */
	public void recover(Forum forum, long now) throws DbException {
		Collection<ForumPostHeader> headers = forumManager.getPostHeaders(forum.getId());
		for (ForumPostHeader header : headers) {
			String text = forumManager.getPostText(header.getId());
			receiver.receive(text, now);
		}
	}
}
