package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyEnvelope;

import java.io.IOException;
import java.util.Base64;

/**
 * Encapsulates a binary emergency envelope in a Briar forum-post text body.
 *
 * <p>Forum posts are already protected by Briar's authorised sharing and
 * cryptographic path. The prefix lets the RescueMesh receiver distinguish an
 * emergency payload from an ordinary forum post without treating every forum
 * message as RescueMesh data.</p>
 */
public final class EmergencyForumPayloadCodec {

	public static final String PREFIX = "rescuemesh:e1:";

	private EmergencyForumPayloadCodec() {}

	public static String encode(EmergencyEnvelope envelope) throws IOException {
		return PREFIX + Base64.getEncoder().encodeToString(
				EmergencyEnvelopeCodec.encode(envelope));
	}

	public static boolean isEmergencyPayload(String text) {
		return text != null && text.startsWith(PREFIX);
	}

	public static EmergencyEnvelope decode(String text) throws IOException {
		if (!isEmergencyPayload(text)) {
			throw new IOException("Not a RescueMesh emergency payload");
		}
		try {
			return EmergencyEnvelopeCodec.decode(Base64.getDecoder().decode(
					text.substring(PREFIX.length())));
		} catch (IllegalArgumentException e) {
			throw new IOException("Invalid Base64 emergency payload", e);
		}
	}
}
