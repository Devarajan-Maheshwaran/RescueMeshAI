package org.briarproject.briar.android.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.briarproject.briar.R;
import org.briarproject.briar.android.fragment.BaseFragment;
import org.rescuemesh.api.emergency.EmergencyDeliveryState;
import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.core.emergency.EmergencyQueueItem;

import java.util.List;

import javax.annotation.Nullable;

/** Local coordinator triage summary; it does not claim external dispatch. */
public class CoordinatorDashboardFragment extends BaseFragment {

	public static CoordinatorDashboardFragment newInstance() {
		return new CoordinatorDashboardFragment();
	}

	@Override
	public String getUniqueTag() { return "org.briarproject.briar.COORDINATOR_DASHBOARD"; }

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle state) {
		return inflater.inflate(R.layout.fragment_rescue_coordinator_dashboard,
				container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle state) {
		super.onViewCreated(view, state);
		requireActivity().setTitle(R.string.rescue_coordinator_title);
		view.findViewById(R.id.rescue_coordinator_open_feed).setOnClickListener(v ->
				showNextFragment(EmergencyFeedFragment.newInstance()));
		render(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getView() != null) render(getView());
	}

	private void render(View view) {
		int critical = 0, high = 0, acknowledged = 0;
		List<EmergencyQueueItem> items = EmergencyRuntime.getQueue()
				.getSnapshot(System.currentTimeMillis());
		for (EmergencyQueueItem item : items) {
			if (item.getEnvelope().getPriority() == EmergencyPriority.CRITICAL) critical++;
			if (item.getEnvelope().getPriority() == EmergencyPriority.HIGH) high++;
			if (item.getState() == EmergencyDeliveryState.ACKNOWLEDGED) acknowledged++;
		}
		((TextView) view.findViewById(R.id.rescue_coordinator_critical)).setText(
				getString(R.string.rescue_coordinator_critical_format, critical));
		((TextView) view.findViewById(R.id.rescue_coordinator_high)).setText(
				getString(R.string.rescue_coordinator_high_format, high));
		((TextView) view.findViewById(R.id.rescue_coordinator_acknowledged)).setText(
				getString(R.string.rescue_coordinator_ack_format, acknowledged));
	}
}
