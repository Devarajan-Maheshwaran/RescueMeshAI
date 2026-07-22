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
import org.briarproject.briar.android.rescue.transport.EmergencyForumRepository;
import org.rescuemesh.core.emergency.EmergencyQueueItem;
import org.briarproject.briar.android.rescue.profile.RescueRole;
import org.briarproject.briar.android.rescue.profile.RescueRoleStore;
import org.briarproject.briar.android.rescue.status.RescueDeviceStatus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Phase-1 RescueMesh home screen.
 *
 * <p>The dashboard intentionally reports only local, verified state. It does
 * not claim that a draft has reached a relay, a rescuer, or an emergency
 * service. Phase 2 replaces the draft card with the persisted emergency-feed
 * read model.</p>
 */
public class RescueDashboardFragment extends BaseFragment {

	private boolean rolePrompted;

	public static final String TAG = "org.briarproject.briar.RESCUE_DASHBOARD";

	public static RescueDashboardFragment newInstance() {
		return new RescueDashboardFragment();
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_rescue_dashboard, container,
				false);
	}

	@Override
	public void onViewCreated(@NonNull View view,
			@Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		BriarApplication application = (BriarApplication) requireActivity()
				.getApplication();
		EmergencyRuntime.start(requireContext(), application.getApplicationComponent());
		requireActivity().setTitle(R.string.rescue_dashboard_title);
		view.findViewById(R.id.rescue_sos_button).setOnClickListener(v ->
				showNextFragment(SosComposerFragment.newCriticalSos()));
		view.findViewById(R.id.rescue_create_update_button).setOnClickListener(v ->
				showNextFragment(SosComposerFragment.newUpdate()));
		view.findViewById(R.id.rescue_choose_role_button).setOnClickListener(v ->
				showNextFragment(RoleSelectionFragment.newInstance()));
		view.findViewById(R.id.rescue_provision_forum_button).setOnClickListener(v ->
				provisionEmergencyForum());
		view.findViewById(R.id.rescue_open_feed_button).setOnClickListener(v ->
				showNextFragment(EmergencyFeedFragment.newInstance()));
		renderLatestDraft(view);
		renderRole(view);
		renderRadioStatus(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		View view = getView();
		if (view != null) {
			renderLatestDraft(view);
			renderRole(view);
			renderRadioStatus(view);
		}
		if (!rolePrompted && new RescueRoleStore(requireContext()).getRole() == null) {
			rolePrompted = true;
			showNextFragment(RoleSelectionFragment.newInstance());
		}
	}

	private void provisionEmergencyForum() {
		BriarApplication application = (BriarApplication) requireActivity()
				.getApplication();
		EmergencyForumRepository repository = new EmergencyForumRepository(
				requireContext(), application.getApplicationComponent().databaseExecutor(),
				application.getApplicationComponent().forumManager());
		repository.getOrCreate(new EmergencyForumRepository.ForumCallback() {
			@Override
			public void onForumReady(org.briarproject.briar.api.forum.Forum forum,
					boolean created) {
				runOnUiThreadUnlessDestroyed(() -> Toast.makeText(requireContext(),
						created ? R.string.rescue_forum_created
								: R.string.rescue_forum_ready, Toast.LENGTH_LONG).show());
			}
			@Override
			public void onFailure(Exception exception) {
				runOnUiThreadUnlessDestroyed(() -> Toast.makeText(requireContext(),
						R.string.rescue_forum_failed, Toast.LENGTH_LONG).show());
			}
		});
	}

	private void renderRole(View view) {
		TextView roleView = view.findViewById(R.id.rescue_role_status);
		RescueRole role = new RescueRoleStore(requireContext()).getRole();
		roleView.setText(role == null ? getString(R.string.rescue_role_not_selected)
				: getString(R.string.rescue_role_selected_format, role.name()));
	}

	private void renderRadioStatus(View view) {
		TextView statusView = view.findViewById(R.id.rescue_radio_status);
		RescueDeviceStatus status = RescueDeviceStatus.read(requireContext());
		int bluetooth = status.isBluetoothAvailable() && status.isBluetoothEnabled()
				? R.string.rescue_radio_on : R.string.rescue_radio_off;
		int wifi = status.isWifiEnabled() ? R.string.rescue_radio_on
				: R.string.rescue_radio_off;
		statusView.setText(getString(R.string.rescue_radio_status_format,
				getString(bluetooth), getString(wifi)));
	}

	private void renderLatestDraft(View view) {
		TextView status = view.findViewById(R.id.rescue_latest_status);
		java.util.List<EmergencyQueueItem> items = EmergencyRuntime.getQueue()
				.getSnapshot(System.currentTimeMillis());
		int critical = 0;
		for (EmergencyQueueItem item : items) {
			if (item.getEnvelope().getPriority()
					== org.rescuemesh.api.emergency.EmergencyPriority.CRITICAL) critical++;
		}
		((TextView) view.findViewById(R.id.rescue_queue_count)).setText(
				getString(R.string.rescue_queue_count_format, items.size()));
		((TextView) view.findViewById(R.id.rescue_critical_count)).setText(
				getString(R.string.rescue_critical_count_format, critical));
		if (items.isEmpty()) {
			status.setText(R.string.rescue_no_local_sos);
			return;
		}
		EmergencyQueueItem item = items.get(0);
		String label = getString(R.string.rescue_latest_queue_format,
				item.getEnvelope().getPriority().name(), item.getState().name(),
				item.getEnvelope().getText());
		status.setText(label);
	}
}
