package org.briarproject.briar.android.rescue.transport;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.briar.api.forum.Forum;
import org.briarproject.briar.api.forum.ForumManager;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 * Locates or creates the single local RescueMesh pilot forum.
 *
 * <p>The saved group ID is not message content or a secret. Forum content and
 * membership remain in Briar's protected database. Creating this forum does
 * not share it automatically: sharing remains an explicit trusted-pilot
 * operation using Briar's existing forum-sharing workflow.</p>
 */
public class EmergencyForumRepository {

	private static final String PREFS = "rescuemesh_emergency_forum";
	private static final String KEY_GROUP_ID = "group_id";
	public static final String DEFAULT_FORUM_NAME = "RescueMesh Emergency Pilot";

	private final SharedPreferences preferences;
	private final Executor databaseExecutor;
	private final ForumManager forumManager;

	public EmergencyForumRepository(Context context,
			@DatabaseExecutor Executor databaseExecutor, ForumManager forumManager) {
		preferences = context.getApplicationContext().getSharedPreferences(PREFS,
				Context.MODE_PRIVATE);
		this.databaseExecutor = databaseExecutor;
		this.forumManager = forumManager;
	}

	public void getOrCreate(ForumCallback callback) {
		databaseExecutor.execute(() -> {
			try {
				Forum existing = getStoredForum();
				if (existing != null) {
					callback.onForumReady(existing, false);
					return;
				}
				Forum created = forumManager.addForum(DEFAULT_FORUM_NAME);
				storeGroupId(created.getId());
				callback.onForumReady(created, true);
			} catch (DbException | IllegalArgumentException e) {
				callback.onFailure(e);
			}
		});
	}

	@Nullable
	private Forum getStoredForum() throws DbException {
		String value = preferences.getString(KEY_GROUP_ID, null);
		if (value == null) return null;
		try {
			byte[] bytes = Base64.decode(value, Base64.NO_WRAP);
			Forum forum = forumManager.getForum(new GroupId(bytes));
			if (forum != null) return forum;
			preferences.edit().remove(KEY_GROUP_ID).apply();
			return null;
		} catch (IllegalArgumentException e) {
			preferences.edit().remove(KEY_GROUP_ID).apply();
			return null;
		}
	}

	private void storeGroupId(GroupId groupId) {
		preferences.edit().putString(KEY_GROUP_ID,
				Base64.encodeToString(groupId.getBytes(), Base64.NO_WRAP)).apply();
	}

	public interface ForumCallback {
		void onForumReady(Forum forum, boolean created);
		void onFailure(Exception exception);
	}
}
