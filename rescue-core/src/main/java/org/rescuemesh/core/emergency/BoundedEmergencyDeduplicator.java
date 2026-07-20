package org.rescuemesh.core.emergency;

import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded, synchronised seen-message cache keyed by stable message ID. */
public class BoundedEmergencyDeduplicator {

	private final int capacity;
	private final LinkedHashMap<String, Long> seen;

	public BoundedEmergencyDeduplicator(final int capacity) {
		if (capacity <= 0) throw new IllegalArgumentException("capacity");
		this.capacity = capacity;
		seen = new LinkedHashMap<String, Long>(capacity + 1, .75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
				return size() > capacity;
			}
		};
	}

	/** @return true when the message ID was already seen and remains cached. */
	public synchronized boolean isDuplicate(String messageId) {
		return seen.containsKey(messageId);
	}

	/** Records an accepted message after full validation. */
	public synchronized void record(String messageId, long seenAt) {
		seen.put(messageId, seenAt);
	}

	public synchronized int size() {
		return seen.size();
	}
}
