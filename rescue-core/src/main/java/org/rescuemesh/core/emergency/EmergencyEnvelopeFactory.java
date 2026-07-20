package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.api.emergency.EmergencyKind;
import org.rescuemesh.api.emergency.EmergencyLocation;
import org.rescuemesh.api.emergency.EmergencyPriority;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Nullable;

/** Creates locally-originated envelopes with a canonical content hash. */
public final class EmergencyEnvelopeFactory {

	private EmergencyEnvelopeFactory() {}

	public static EmergencyEnvelope create(String originId, EmergencyKind kind,
			EmergencyPriority priority, String text, @Nullable Integer victimCount,
			@Nullable EmergencyLocation location, long now, long expiresAt,
			int hopLimit) {
		EmergencyEnvelope unsigned = new EmergencyEnvelope(
				EmergencyEnvelope.SCHEMA_VERSION_1, UUID.randomUUID().toString(),
				originId, now, expiresAt, kind, priority, text, victimCount, location,
				0, hopLimit, new byte[EmergencyEnvelopeCodec.HASH_BYTES]);
		try {
			byte[] hash = EmergencyEnvelopeCodec.sha256(
					EmergencyEnvelopeCodec.encodeContent(unsigned));
			return new EmergencyEnvelope(unsigned.getSchemaVersion(),
					unsigned.getMessageId(), unsigned.getOriginId(), unsigned.getCreatedAt(),
					unsigned.getExpiresAt(), unsigned.getKind(), unsigned.getPriority(),
					unsigned.getText(), unsigned.getVictimCount(), unsigned.getLocation(),
					unsigned.getHopCount(), unsigned.getHopLimit(), hash);
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid envelope content", e);
		}
	}
}
