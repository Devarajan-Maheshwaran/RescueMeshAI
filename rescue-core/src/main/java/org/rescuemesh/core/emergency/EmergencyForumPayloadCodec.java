package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyEnvelope;

import java.io.IOException;

/**
 * Encapsulates a binary emergency envelope in a Briar forum-post text body.
 *
 * <p>Hex encoding is deliberately used instead of a platform Base64 API so
 * the payload works on Briar's Android API 21 baseline without relying on a
 * Java-library desugaring configuration. Forum posts are already protected by
 * Briar's authorised sharing and cryptographic path.</p>
 */
public final class EmergencyForumPayloadCodec {

	public static final String PREFIX = "rescuemesh:e1:";
	private static final char[] HEX = "0123456789abcdef".toCharArray();

	private EmergencyForumPayloadCodec() {}

	public static String encode(EmergencyEnvelope envelope) throws IOException {
		byte[] bytes = EmergencyEnvelopeCodec.encode(envelope);
		char[] encoded = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int value = bytes[i] & 0xFF;
			encoded[i * 2] = HEX[value >>> 4];
			encoded[i * 2 + 1] = HEX[value & 0x0F];
		}
		return PREFIX + new String(encoded);
	}

	public static boolean isEmergencyPayload(String text) {
		return text != null && text.startsWith(PREFIX);
	}

	public static EmergencyEnvelope decode(String text) throws IOException {
		if (!isEmergencyPayload(text)) {
			throw new IOException("Not a RescueMesh emergency payload");
		}
		String encoded = text.substring(PREFIX.length());
		if ((encoded.length() & 1) != 0) throw new IOException("Invalid hex payload");
		byte[] bytes = new byte[encoded.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			int high = Character.digit(encoded.charAt(i * 2), 16);
			int low = Character.digit(encoded.charAt(i * 2 + 1), 16);
			if (high < 0 || low < 0) throw new IOException("Invalid hex payload");
			bytes[i] = (byte) ((high << 4) | low);
		}
		return EmergencyEnvelopeCodec.decode(bytes);
	}
}
