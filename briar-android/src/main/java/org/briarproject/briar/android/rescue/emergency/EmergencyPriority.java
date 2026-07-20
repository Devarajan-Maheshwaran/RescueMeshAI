package org.briarproject.briar.android.rescue.emergency;

/**
 * The sender-selected urgency of an emergency envelope.
 *
 * <p>This is deliberately separate from future AI suggestions: a classifier
 * must never silently downgrade a priority selected by the person creating an
 * SOS.</p>
 */
public enum EmergencyPriority {
	CRITICAL,
	HIGH,
	NORMAL
}
