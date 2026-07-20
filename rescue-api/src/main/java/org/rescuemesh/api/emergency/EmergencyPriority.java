package org.rescuemesh.api.emergency;

/** Sender-selected urgency. Automatic classification may suggest, never silently lower it. */
public enum EmergencyPriority {
	CRITICAL,
	HIGH,
	NORMAL
}
