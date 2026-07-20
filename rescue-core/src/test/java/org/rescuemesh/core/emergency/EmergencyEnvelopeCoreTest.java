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
				"Different text", null, null, 0, 3, created.getContentHash());
		assertFalse(EmergencyEnvelopeValidator.validate(altered, NOW).isValid());
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
