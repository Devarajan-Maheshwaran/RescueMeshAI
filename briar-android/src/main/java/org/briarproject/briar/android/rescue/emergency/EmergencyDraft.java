package org.briarproject.briar.android.rescue.emergency;

import org.rescuemesh.api.emergency.EmergencyPriority;

import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Local Phase-1 representation of a user-created SOS.
 *
 * <p>Phase 2 replaces the persistence format with the authenticated,
 * versioned EmergencyEnvelopeV1 protocol. This small value object exists so
 * that the emergency UI has an explicit, testable local hand-off and does not
 * pretend that an SOS has already been transmitted.</p>
 */
public class EmergencyDraft {

	private final String id;
	private final long createdAt;
	private final EmergencyPriority priority;
	private final String message;
	@Nullable
	private final Integer victimCount;
	private final boolean locationRequested;

	public EmergencyDraft(String id, long createdAt, EmergencyPriority priority,
			String message, @Nullable Integer victimCount,
			boolean locationRequested) {
		this.id = id;
		this.createdAt = createdAt;
		this.priority = priority;
		this.message = message;
		this.victimCount = victimCount;
		this.locationRequested = locationRequested;
	}

	public static EmergencyDraft create(EmergencyPriority priority, String message,
			@Nullable Integer victimCount, boolean locationRequested) {
		return new EmergencyDraft(UUID.randomUUID().toString(),
				System.currentTimeMillis(), priority, message, victimCount,
				locationRequested);
	}

	public String getId() {
		return id;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public EmergencyPriority getPriority() {
		return priority;
	}

	public String getMessage() {
		return message;
	}

	@Nullable
	public Integer getVictimCount() {
		return victimCount;
	}

	public boolean isLocationRequested() {
		return locationRequested;
	}
}
