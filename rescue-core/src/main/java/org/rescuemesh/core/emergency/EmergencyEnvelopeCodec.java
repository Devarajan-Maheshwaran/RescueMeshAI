package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.api.emergency.EmergencyKind;
import org.rescuemesh.api.emergency.EmergencyLocation;
import org.rescuemesh.api.emergency.EmergencyPriority;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nullable;

/** Canonical binary encoding used for size checks, content hashes and transport payloads. */
public final class EmergencyEnvelopeCodec {

	private static final int MAGIC = 0x524D4531; // RME1
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	public static final int MAX_TEXT_BYTES = 500;
	public static final int MAX_IDENTIFIER_BYTES = 128;
	public static final int HASH_BYTES = 32;

	private EmergencyEnvelopeCodec() {}

	public static byte[] encode(EmergencyEnvelope envelope) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeInt(MAGIC);
		writeContent(out, envelope);
		byte[] hash = envelope.getContentHash();
		if (hash.length != HASH_BYTES) throw new IOException("Invalid content hash");
		out.write(hash);
		out.flush();
		return bytes.toByteArray();
	}

	public static byte[] encodeContent(EmergencyEnvelope envelope)
			throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		writeContent(out, envelope);
		out.flush();
		return bytes.toByteArray();
	}

	public static EmergencyEnvelope decode(byte[] bytes) throws IOException {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
		if (in.readInt() != MAGIC) throw new IOException("Unknown RescueMesh envelope");
		int schema = in.readInt();
		String messageId = readString(in, MAX_IDENTIFIER_BYTES);
		String originId = readString(in, MAX_IDENTIFIER_BYTES);
		long createdAt = in.readLong();
		long expiresAt = in.readLong();
		EmergencyKind kind = enumByName(EmergencyKind.class, readString(in, 16));
		EmergencyPriority priority = enumByName(EmergencyPriority.class,
				readString(in, 16));
		String text = readString(in, MAX_TEXT_BYTES);
		String relatedMessageId = in.readBoolean()
				? readString(in, MAX_IDENTIFIER_BYTES) : null;
		Integer victimCount = in.readBoolean() ? in.readInt() : null;
		EmergencyLocation location = null;
		if (in.readBoolean()) {
			location = new EmergencyLocation(in.readDouble(), in.readDouble(),
					in.readFloat(), in.readLong());
		}
		int hopCount = in.readInt();
		int hopLimit = in.readInt();
		byte[] hash = new byte[HASH_BYTES];
		in.readFully(hash);
		if (in.available() != 0) throw new IOException("Trailing envelope data");
		return new EmergencyEnvelope(schema, messageId, originId, createdAt,
				expiresAt, kind, priority, text, relatedMessageId, victimCount, location,
				hopCount, hopLimit, hash);
	}

	public static byte[] sha256(byte[] content) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(content);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError("SHA-256 missing", e);
		}
	}

	private static void writeContent(DataOutputStream out, EmergencyEnvelope e)
			throws IOException {
		out.writeInt(e.getSchemaVersion());
		writeString(out, e.getMessageId(), MAX_IDENTIFIER_BYTES);
		writeString(out, e.getOriginId(), MAX_IDENTIFIER_BYTES);
		out.writeLong(e.getCreatedAt());
		out.writeLong(e.getExpiresAt());
		writeString(out, e.getKind().name(), 16);
		writeString(out, e.getPriority().name(), 16);
		writeString(out, e.getText(), MAX_TEXT_BYTES);
		String relatedMessageId = e.getRelatedMessageId();
		out.writeBoolean(relatedMessageId != null);
		if (relatedMessageId != null) {
			writeString(out, relatedMessageId, MAX_IDENTIFIER_BYTES);
		}
		Integer victimCount = e.getVictimCount();
		out.writeBoolean(victimCount != null);
		if (victimCount != null) out.writeInt(victimCount);
		EmergencyLocation location = e.getLocation();
		out.writeBoolean(location != null);
		if (location != null) {
			out.writeDouble(location.getLatitude());
			out.writeDouble(location.getLongitude());
			out.writeFloat(location.getAccuracyMetres());
			out.writeLong(location.getCapturedAt());
		}
		out.writeInt(e.getHopCount());
		out.writeInt(e.getHopLimit());
	}

	private static void writeString(DataOutputStream out, String value, int max)
			throws IOException {
		byte[] bytes = value.getBytes(UTF_8);
		if (bytes.length > max) throw new IOException("String too long");
		out.writeShort(bytes.length);
		out.write(bytes);
	}

	private static String readString(DataInputStream in, int max) throws IOException {
		int length = in.readUnsignedShort();
		if (length > max) throw new IOException("String too long");
		byte[] bytes = new byte[length];
		in.readFully(bytes);
		return new String(bytes, UTF_8);
	}

	private static <E extends Enum<E>> E enumByName(Class<E> type, String value)
			throws IOException {
		try {
			return Enum.valueOf(type, value);
		} catch (IllegalArgumentException e) {
			throw new IOException("Unknown enum value", e);
		}
	}
}
