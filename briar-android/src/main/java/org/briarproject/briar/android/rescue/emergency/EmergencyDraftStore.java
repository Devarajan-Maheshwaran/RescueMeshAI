package org.briarproject.briar.android.rescue.emergency;

import android.content.Context;

import javax.annotation.Nullable;

/**
 * Non-persistent Phase-1 UI hand-off store.
 *
 * <p>Emergency text and location must not be placed in ordinary Android
 * SharedPreferences, which are not an acceptable encrypted message store.
 * Until Phase 2 maps validated EmergencyEnvelopeV1 data into Briar's protected
 * persistence/synchronisation path, this class retains only the current app
 * session draft and never reports it as sent.</p>
 */
public class EmergencyDraftStore {

	@Nullable
	private static volatile EmergencyDraft latest;

	public EmergencyDraftStore(Context context) {
		// Kept as an Android-facing constructor so Phase 2 can replace this
		// implementation with a secure injected repository without changing UI.
	}

	public void save(EmergencyDraft draft) {
		latest = draft;
	}

	@Nullable
	public EmergencyDraft getLatest() {
		return latest;
	}
}
