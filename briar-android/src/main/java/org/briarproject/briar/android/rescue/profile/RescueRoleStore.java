package org.briarproject.briar.android.rescue.profile;

import android.content.Context;
import android.content.SharedPreferences;

import javax.annotation.Nullable;

/** Stores only the non-sensitive local UI role preference. */
public class RescueRoleStore {

	private static final String PREFS = "rescuemesh_profile";
	private static final String KEY_ROLE = "role";

	private final SharedPreferences preferences;

	public RescueRoleStore(Context context) {
		preferences = context.getApplicationContext().getSharedPreferences(PREFS,
				Context.MODE_PRIVATE);
	}

	public void setRole(RescueRole role) {
		preferences.edit().putString(KEY_ROLE, role.name()).apply();
	}

	@Nullable
	public RescueRole getRole() {
		String value = preferences.getString(KEY_ROLE, null);
		if (value == null) return null;
		try {
			return RescueRole.valueOf(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
