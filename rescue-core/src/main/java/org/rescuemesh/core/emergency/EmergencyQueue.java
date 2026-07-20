package org.rescuemesh.core.emergency;

import org.rescuemesh.api.emergency.EmergencyDeliveryState;
import org.rescuemesh.api.emergency.EmergencyEnvelope;
import org.rescuemesh.api.emergency.EmergencyPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe local lifecycle and priority queue.
 *
 * <p>This is deliberately transport-agnostic. A Briar adapter will call
 * {@link #markSynchronisedToPeer(String, long)} only after an actual successful
 * authorised synchronisation, and never merely after pressing Send.</p>
 */
public class EmergencyQueue {

	private final BoundedEmergencyDeduplicator deduplicator;
	private final Map<String, EmergencyQueueItem> items = new LinkedHashMap<>();

	public EmergencyQueue(int seenCacheCapacity) {
		deduplicator = new BoundedEmergencyDeduplicator(seenCacheCapacity);
	}

	public synchronized AdmissionResult admit(EmergencyEnvelope envelope, long now,
			boolean locallyCreated) {
		EmergencyEnvelopeValidator.ValidationResult validation =
				EmergencyEnvelopeValidator.validate(envelope, now);
		if (!validation.isValid()) {
			return AdmissionResult.rejected(validation.getState(), validation.getReason());
		}
		if (deduplicator.isDuplicate(envelope.getMessageId())) {
			return AdmissionResult.duplicate();
		}
		deduplicator.record(envelope.getMessageId(), now);
		EmergencyDeliveryState initial = locallyCreated
				? EmergencyDeliveryState.STORED : EmergencyDeliveryState.QUEUED;
		EmergencyQueueItem item = new EmergencyQueueItem(envelope, now, initial);
		items.put(envelope.getMessageId(), item);
		return AdmissionResult.accepted(item);
	}

	public synchronized List<EmergencyQueueItem> getEligibleForTransfer(long now) {
		expireDueItems(now);
		List<EmergencyQueueItem> eligible = new ArrayList<>();
		for (EmergencyQueueItem item : items.values()) {
			if (item.getState() == EmergencyDeliveryState.STORED
					|| item.getState() == EmergencyDeliveryState.QUEUED
					|| item.getState() == EmergencyDeliveryState.SYNCHRONISED_TO_PEER) {
				eligible.add(item);
			}
		}
		Collections.sort(eligible, new PriorityThenAgeComparator());
		return Collections.unmodifiableList(eligible);
	}

	public synchronized boolean markQueued(String messageId, long now) {
		EmergencyQueueItem item = items.get(messageId);
		if (item == null || isFinal(item.getState())) return false;
		item.transitionTo(EmergencyDeliveryState.QUEUED, now);
		return true;
	}

	public synchronized boolean markSynchronisedToPeer(String messageId, long now) {
		EmergencyQueueItem item = items.get(messageId);
		if (item == null || isFinal(item.getState())) return false;
		item.incrementSynchronisedPeerCount(now);
		return true;
	}

	public synchronized boolean markAcknowledged(String messageId, long now) {
		EmergencyQueueItem item = items.get(messageId);
		if (item == null || item.getState() == EmergencyDeliveryState.EXPIRED
				|| item.getState() == EmergencyDeliveryState.REJECTED) return false;
		item.transitionTo(EmergencyDeliveryState.ACKNOWLEDGED, now);
		return true;
	}

	public synchronized int expireDueItems(long now) {
		int expired = 0;
		for (EmergencyQueueItem item : items.values()) {
			if (!isFinal(item.getState()) && item.getEnvelope().getExpiresAt() <= now) {
				item.transitionTo(EmergencyDeliveryState.EXPIRED, now);
				expired++;
			}
		}
		return expired;
	}

	private static boolean isFinal(EmergencyDeliveryState state) {
		return state == EmergencyDeliveryState.ACKNOWLEDGED
				|| state == EmergencyDeliveryState.EXPIRED
				|| state == EmergencyDeliveryState.REJECTED;
	}

	public static final class AdmissionResult {
		private final EmergencyQueueItem item;
		private final boolean duplicate;
		private final EmergencyEnvelopeValidator.ValidationResult.State rejectionState;
		private final String reason;
		private AdmissionResult(EmergencyQueueItem item, boolean duplicate,
				EmergencyEnvelopeValidator.ValidationResult.State rejectionState,
				String reason) {
			this.item = item;
			this.duplicate = duplicate;
			this.rejectionState = rejectionState;
			this.reason = reason;
		}
		static AdmissionResult accepted(EmergencyQueueItem item) {
			return new AdmissionResult(item, false, null, "");
		}
		static AdmissionResult duplicate() {
			return new AdmissionResult(null, true, null, "Duplicate message");
		}
		static AdmissionResult rejected(EmergencyEnvelopeValidator.ValidationResult.State state,
				String reason) {
			return new AdmissionResult(null, false, state, reason);
		}
		public boolean isAccepted() { return item != null; }
		public boolean isDuplicate() { return duplicate; }
		public EmergencyQueueItem getItem() { return item; }
		public EmergencyEnvelopeValidator.ValidationResult.State getRejectionState() { return rejectionState; }
		public String getReason() { return reason; }
	}

	private static class PriorityThenAgeComparator
			implements Comparator<EmergencyQueueItem> {
		@Override
		public int compare(EmergencyQueueItem first, EmergencyQueueItem second) {
			int priority = priorityWeight(second.getEnvelope().getPriority())
					- priorityWeight(first.getEnvelope().getPriority());
			if (priority != 0) return priority;
			return Long.compare(first.getReceivedAt(), second.getReceivedAt());
		}
		private int priorityWeight(EmergencyPriority priority) {
			if (priority == EmergencyPriority.CRITICAL) return 3;
			if (priority == EmergencyPriority.HIGH) return 2;
			return 1;
		}
	}
}
