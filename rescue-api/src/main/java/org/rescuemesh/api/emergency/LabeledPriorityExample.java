package org.rescuemesh.api.emergency;

/** A de-identified English-only labelled example for offline evaluation. */
public final class LabeledPriorityExample {

	private final String id;
	private final String text;
	private final EmergencyPriority expectedPriority;

	public LabeledPriorityExample(String id, String text,
			EmergencyPriority expectedPriority) {
		if (id == null || id.length() == 0) throw new IllegalArgumentException("id");
		if (text == null) throw new NullPointerException("text");
		if (expectedPriority == null) throw new NullPointerException("expectedPriority");
		this.id = id;
		this.text = text;
		this.expectedPriority = expectedPriority;
	}

	public String getId() { return id; }
	public String getText() { return text; }
	public EmergencyPriority getExpectedPriority() { return expectedPriority; }
}
