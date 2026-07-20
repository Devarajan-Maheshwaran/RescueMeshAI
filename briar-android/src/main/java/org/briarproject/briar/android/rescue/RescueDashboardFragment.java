package org.briarproject.briar.android.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.briarproject.briar.R;
import org.briarproject.briar.android.fragment.BaseFragment;
import org.briarproject.briar.android.rescue.emergency.EmergencyDraft;
import org.briarproject.briar.android.rescue.emergency.EmergencyDraftStore;
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
		requireActivity().setTitle(R.string.rescue_dashboard_title);
		view.findViewById(R.id.rescue_sos_button).setOnClickListener(v ->
				showNextFragment(SosComposerFragment.newCriticalSos()));
		view.findViewById(R.id.rescue_create_update_button).setOnClickListener(v ->
				showNextFragment(SosComposerFragment.newUpdate()));
		view.findViewById(R.id.rescue_choose_role_button).setOnClickListener(v ->
				showNextFragment(RoleSelectionFragment.newInstance()));
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
		EmergencyDraft draft = new EmergencyDraftStore(requireContext()).getLatest();
		if (draft == null) {
			status.setText(R.string.rescue_no_local_sos);
			return;
		}
		String label = getString(R.string.rescue_latest_draft_format,
				draft.getPriority().name(), draft.getMessage());
		status.setText(label);
	}
}
