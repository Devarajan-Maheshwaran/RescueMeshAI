package org.rescuemesh.core.emergency;

import org.junit.Test;
import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.api.emergency.EmergencyKind;
import org.rescuemesh.api.emergency.EmergencyPriority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmergencyEnvelopeCoreTest {

	private static final long NOW = 1_800_000_000_000L;

	@Test
	public void roundTripRetainsEnvelopeAndValidHash() throws Exception {
		EmergencyEnvelope created = EmergencyEnvelopeFactory.create("origin-a",
				EmergencyKind.SOS, EmergencyPriority.CRITICAL, "Trapped near bridge",
				2, null, NOW, NOW + 60_000L, 3);
		EmergencyEnvelope decoded = EmergencyEnvelopeCodec.decode(
				EmergencyEnvelopeCodec.encode(created));

		assertEquals(created.getMessageId(), decoded.getMessageId());
		assertEquals("Trapped near bridge", decoded.getText());
		assertTrue(EmergencyEnvelopeValidator.validate(decoded, NOW).isValid());
	}

	@Test
	public void modifiedContentFailsHashValidation() {
		EmergencyEnvelope created = EmergencyEnvelopeFactory.create("origin-a",
				EmergencyKind.SOS, EmergencyPriority.HIGH, "Need help", null, null,
				NOW, NOW + 60_000L, 3);
		EmergencyEnvelope altered = new EmergencyEnvelope(created.getSchemaVersion(),
				created.getMessageId(), created.getOriginId(), created.getCreatedAt(),
				created.getExpiresAt(), created.getKind(), created.getPriority(),
				"Different text", null, null, null, 0, 3, created.getContentHash());
		assertFalse(EmergencyEnvelopeValidator.validate(altered, NOW).isValid());
	}

	@Test
	public void acknowledgementHasLinkedMessageIdAndValidHash() throws Exception {
		EmergencyEnvelope acknowledgement = EmergencyEnvelopeFactory.createAcknowledgement(
				"origin-a", "sos-message-id", NOW, NOW + 60_000L);
		assertEquals(EmergencyKind.ACK, acknowledgement.getKind());
		assertEquals("sos-message-id", acknowledgement.getRelatedMessageId());
		assertTrue(EmergencyEnvelopeValidator.validate(acknowledgement, NOW).isValid());
		assertEquals("sos-message-id", EmergencyEnvelopeCodec.decode(
				EmergencyEnvelopeCodec.encode(acknowledgement)).getRelatedMessageId());
	}

	@Test
	public void forumPayloadRoundTripUsesExplicitPrefix() throws Exception {
		EmergencyEnvelope envelope = EmergencyEnvelopeFactory.create("origin-a",
				EmergencyKind.SOS, EmergencyPriority.CRITICAL, "Help", null, null,
				NOW, NOW + 60_000L, 3);
		String payload = EmergencyForumPayloadCodec.encode(envelope);
		assertTrue(EmergencyForumPayloadCodec.isEmergencyPayload(payload));
		assertEquals(envelope.getMessageId(), EmergencyForumPayloadCodec.decode(payload)
				.getMessageId());
		assertFalse(EmergencyForumPayloadCodec.isEmergencyPayload("ordinary forum post"));
	}

	@Test
	public void queueOrdersCriticalMessagesAndOnlyMarksActualPeerSync() {
		EmergencyQueue queue = new EmergencyQueue(10);
		EmergencyEnvelope normal = EmergencyEnvelopeFactory.create("origin-a",
				EmergencyKind.STATUS, EmergencyPriority.NORMAL, "Safe", null, null,
				NOW, NOW + 60_000L, 3);
		EmergencyEnvelope critical = EmergencyEnvelopeFactory.create("origin-b",
				EmergencyKind.SOS, EmergencyPriority.CRITICAL, "Trapped", null, null,
				NOW + 1, NOW + 60_000L, 3);
		assertTrue(queue.admit(normal, NOW, true).isAccepted());
		assertTrue(queue.admit(critical, NOW + 1, true).isAccepted());
		assertEquals(critical.getMessageId(),
				queue.getEligibleForTransfer(NOW + 1).get(0).getEnvelope().getMessageId());
		assertTrue(queue.markSynchronisedToPeer(critical.getMessageId(), NOW + 2));
		assertEquals(1, queue.getEligibleForTransfer(NOW + 2).get(0)
				.getSynchronisedPeerCount());
		assertTrue(queue.admit(critical, NOW + 2, false).isDuplicate());
	}

	@Test
	public void boundedDeduplicatorEvictsOldestEntry() {
		BoundedEmergencyDeduplicator dedup = new BoundedEmergencyDeduplicator(2);
		dedup.record("a", NOW);
		dedup.record("b", NOW);
		dedup.record("c", NOW);
		assertFalse(dedup.isDuplicate("a"));
		assertTrue(dedup.isDuplicate("b"));
		assertTrue(dedup.isDuplicate("c"));
	}
}
