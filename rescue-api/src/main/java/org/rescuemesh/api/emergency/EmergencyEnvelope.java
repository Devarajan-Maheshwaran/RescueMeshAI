package org.rescuemesh.api.emergency;

import java.util.Arrays;

import javax.annotation.Nullable;

/**
 * Immutable versioned RescueMesh emergency payload.
 *
 * <p>The envelope deliberately contains no mutable routing state. A receiver's
 * queue state, seen peers and classifier result are local operational metadata.
 * Authentication material is introduced with the admission/session design; the
 * Phase-2 content hash is already present to support idempotent ingestion.</p>
 */
public final class EmergencyEnvelope {

	public static final int SCHEMA_VERSION_1 = 1;

	private final int schemaVersion;
	private final String messageId;
	private final String originId;
	private final long createdAt;
	private final long expiresAt;
	private final EmergencyKind kind;
	private final EmergencyPriority priority;
	private final String text;
	@Nullable
	private final String relatedMessageId;
	@Nullable
	private final Integer victimCount;
	@Nullable
	private final EmergencyLocation location;
	private final int hopCount;
	private final int hopLimit;
	private final byte[] contentHash;

	public EmergencyEnvelope(int schemaVersion, String messageId, String originId,
			long createdAt, long expiresAt, EmergencyKind kind,
			EmergencyPriority priority, String text, @Nullable String relatedMessageId,
			@Nullable Integer victimCount, @Nullable EmergencyLocation location,
			int hopCount, int hopLimit,
			byte[] contentHash) {
		this.schemaVersion = schemaVersion;
		this.messageId = requireNonEmpty(messageId, "messageId");
		this.originId = requireNonEmpty(originId, "originId");
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		if (kind == null || priority == null) throw new NullPointerException();
		this.kind = kind;
		this.priority = priority;
		this.text = text == null ? "" : text;
		this.relatedMessageId = relatedMessageId;
		this.victimCount = victimCount;
		this.location = location;
		this.hopCount = hopCount;
		this.hopLimit = hopLimit;
		if (contentHash == null) throw new NullPointerException("contentHash");
		this.contentHash = Arrays.copyOf(contentHash, contentHash.length);
	}

	private static String requireNonEmpty(String value, String name) {
		if (value == null || value.length() == 0) {
			throw new IllegalArgumentException(name + " must not be empty");
		}
		return value;
	}

	public int getSchemaVersion() { return schemaVersion; }
	public String getMessageId() { return messageId; }
	public String getOriginId() { return originId; }
	public long getCreatedAt() { return createdAt; }
	public long getExpiresAt() { return expiresAt; }
	public EmergencyKind getKind() { return kind; }
	public EmergencyPriority getPriority() { return priority; }
	public String getText() { return text; }
	@Nullable public String getRelatedMessageId() { return relatedMessageId; }
	@Nullable public Integer getVictimCount() { return victimCount; }
	@Nullable public EmergencyLocation getLocation() { return location; }
	public int getHopCount() { return hopCount; }
	public int getHopLimit() { return hopLimit; }
	public byte[] getContentHash() {
		return Arrays.copyOf(contentHash, contentHash.length);
	}
}
