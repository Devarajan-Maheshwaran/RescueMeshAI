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
import org.rescuemesh.core.emergency.EmergencyQueueItem;

import java.util.List;

import javax.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/** Priority card feed for locally queued and trusted-forum emergency data. */
public class EmergencyFeedFragment extends BaseFragment
		implements EmergencyFeedAdapter.ItemListener {

	private EmergencyFeedAdapter adapter;

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
		RecyclerView list = view.findViewById(R.id.rescue_feed_list);
		list.setLayoutManager(new LinearLayoutManager(requireContext()));
		adapter = new EmergencyFeedAdapter(this);
		list.setAdapter(adapter);
		render(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getView() != null) render(getView());
	}

	private void render(View view) {
		List<EmergencyQueueItem> items = EmergencyRuntime.getQueue()
				.getSnapshot(System.currentTimeMillis());
		adapter.replaceItems(items);
		view.findViewById(R.id.rescue_feed_empty).setVisibility(
				items.isEmpty() ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onAcknowledge(EmergencyQueueItem item) {
		RescueRole role = new RescueRoleStore(requireContext()).getRole();
		if (role != RescueRole.RESCUER && role != RescueRole.COORDINATOR) {
			Toast.makeText(requireContext(), R.string.rescue_ack_role_required,
					Toast.LENGTH_LONG).show();
			return;
		}
		BriarApplication app = (BriarApplication) requireActivity().getApplication();
		EmergencyAcknowledgementController controller =
				new EmergencyAcknowledgementController(requireContext(),
						app.getApplicationComponent().databaseExecutor(),
						app.getApplicationComponent().cryptoExecutor(),
						app.getApplicationComponent().identityManager(),
						app.getApplicationComponent().forumManager(),
						app.getApplicationComponent().clock(), EmergencyRuntime.getQueue());
		controller.acknowledge(item.getEnvelope().getMessageId(),
				new EmergencyAcknowledgementController.Callback() {
					@Override
					public void onAcknowledgementQueued(
							org.rescuemesh.api.emergency.EmergencyEnvelope acknowledgement) {
						runOnUiThreadUnlessDestroyed(() -> {
							Toast.makeText(requireContext(), R.string.rescue_ack_queued,
									Toast.LENGTH_LONG).show();
							render(requireView());
						});
					}
					@Override
					public void onFailure(Exception exception) {
						runOnUiThreadUnlessDestroyed(() -> Toast.makeText(requireContext(),
								R.string.rescue_ack_failed, Toast.LENGTH_LONG).show());
					}
				});
	}
}
