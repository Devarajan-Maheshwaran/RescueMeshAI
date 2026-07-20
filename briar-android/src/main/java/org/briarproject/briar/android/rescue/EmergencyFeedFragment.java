package org.briarproject.briar.android.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.briarproject.briar.R;
import org.briarproject.briar.android.BriarApplication;
import org.briarproject.briar.android.fragment.BaseFragment;
import org.briarproject.briar.android.rescue.profile.RescueRole;
import org.briarproject.briar.android.rescue.profile.RescueRoleStore;
import org.briarproject.briar.android.rescue.transport.EmergencyAcknowledgementController;
import org.rescuemesh.api.emergency.EmergencyDeliveryState;
import org.rescuemesh.api.emergency.EmergencyKind;
import org.rescuemesh.core.emergency.EmergencyQueueItem;

import java.util.List;

import javax.annotation.Nullable;

/** Phase-2 priority feed for locally queued and accepted trusted-forum SOS data. */
public class EmergencyFeedFragment extends BaseFragment {

	public static EmergencyFeedFragment newInstance() { return new EmergencyFeedFragment(); }

	@Override
	public String getUniqueTag() { return "org.briarproject.briar.RESCUE_FEED"; }

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle state) {
		return inflater.inflate(R.layout.fragment_rescue_emergency_feed, container,
				false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle state) {
		super.onViewCreated(view, state);
		requireActivity().setTitle(R.string.rescue_feed_title);
		view.findViewById(R.id.rescue_acknowledge_button).setOnClickListener(v ->
				acknowledgeHighestEligible(view));
		render(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getView() != null) render(getView());
	}

	private void render(View view) {
		TextView feed = view.findViewById(R.id.rescue_feed_text);
		List<EmergencyQueueItem> items = EmergencyRuntime.getQueue()
				.getSnapshot(System.currentTimeMillis());
		if (items.isEmpty()) {
			feed.setText(R.string.rescue_feed_empty);
			return;
		}
		StringBuilder text = new StringBuilder();
		for (EmergencyQueueItem item : items) {
			text.append(item.getEnvelope().getPriority().name()).append(" · ")
					.append(item.getState().name()).append('\n')
					.append(item.getEnvelope().getText()).append("\n\n");
		}
		feed.setText(text.toString().trim());
	}

	private void acknowledgeHighestEligible(View view) {
		RescueRole role = new RescueRoleStore(requireContext()).getRole();
		if (role != RescueRole.RESCUER && role != RescueRole.COORDINATOR) {
			Toast.makeText(requireContext(), R.string.rescue_ack_role_required,
					Toast.LENGTH_LONG).show();
			return;
		}
		EmergencyQueueItem target = null;
		for (EmergencyQueueItem item : EmergencyRuntime.getQueue()
				.getSnapshot(System.currentTimeMillis())) {
			if ((item.getEnvelope().getKind() == EmergencyKind.SOS
					|| item.getEnvelope().getKind() == EmergencyKind.UPDATE)
					&& item.getState() != EmergencyDeliveryState.ACKNOWLEDGED
					&& item.getState() != EmergencyDeliveryState.EXPIRED) {
				target = item;
				break;
			}
		}
		if (target == null) {
			Toast.makeText(requireContext(), R.string.rescue_no_ack_target,
					Toast.LENGTH_SHORT).show();
			return;
		}
		final EmergencyQueueItem selected = target;
		view.findViewById(R.id.rescue_acknowledge_button).setEnabled(false);
		BriarApplication app = (BriarApplication) requireActivity().getApplication();
		EmergencyAcknowledgementController controller =
				new EmergencyAcknowledgementController(requireContext(),
						app.getApplicationComponent().databaseExecutor(),
						app.getApplicationComponent().cryptoExecutor(),
						app.getApplicationComponent().identityManager(),
						app.getApplicationComponent().forumManager(),
						app.getApplicationComponent().clock(), EmergencyRuntime.getQueue());
		controller.acknowledge(selected.getEnvelope().getMessageId(),
				new EmergencyAcknowledgementController.Callback() {
					@Override
					public void onAcknowledgementQueued(
							org.rescuemesh.api.emergency.EmergencyEnvelope acknowledgement) {
						runOnUiThreadUnlessDestroyed(() -> {
							Toast.makeText(requireContext(), R.string.rescue_ack_queued,
									Toast.LENGTH_LONG).show();
							view.findViewById(R.id.rescue_acknowledge_button).setEnabled(true);
							render(view);
						});
					}
					@Override
					public void onFailure(Exception exception) {
						runOnUiThreadUnlessDestroyed(() -> {
							Toast.makeText(requireContext(), R.string.rescue_ack_failed,
									Toast.LENGTH_LONG).show();
							view.findViewById(R.id.rescue_acknowledge_button).setEnabled(true);
						});
					}
				});
	}
}
