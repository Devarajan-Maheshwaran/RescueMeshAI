package org.briarproject.briar.android.rescue.transport;

import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.event.EventListener;
import org.briarproject.bramble.api.system.Clock;
import org.briarproject.briar.api.forum.event.ForumPostReceivedEvent;

/** Routes incoming posts from only the configured trusted emergency forum. */
public class EmergencyForumEventListener implements EventListener {

	private final EmergencyForumRepository forumRepository;
	private final BriarEmergencyForumReceiver receiver;
	private final Clock clock;
	private final InboundListener inboundListener;

	public EmergencyForumEventListener(EmergencyForumRepository forumRepository,
			BriarEmergencyForumReceiver receiver, Clock clock,
			InboundListener inboundListener) {
		this.forumRepository = forumRepository;
		this.receiver = receiver;
		this.clock = clock;
		this.inboundListener = inboundListener;
	}

	@Override
	public void eventOccurred(Event event) {
		if (!(event instanceof ForumPostReceivedEvent)) return;
		ForumPostReceivedEvent post = (ForumPostReceivedEvent) event;
		if (!forumRepository.isConfiguredForum(post.getGroupId())) return;
		BriarEmergencyForumReceiver.ReceiveResult result = receiver.receive(
				post.getText(), clock.currentTimeMillis());
		if (result.getType() == BriarEmergencyForumReceiver.ReceiveResult.Type.ACCEPTED) {
			inboundListener.onEmergencyAccepted(result.getEnvelope());
		} else if (result.getType()
				== BriarEmergencyForumReceiver.ReceiveResult.Type.REJECTED) {
			inboundListener.onEmergencyRejected(result.getReason());
		}
	}

	public interface InboundListener {
		void onEmergencyAccepted(org.rescuemesh.api.emergency.EmergencyEnvelope envelope);
		void onEmergencyRejected(String reason);
	}
}
