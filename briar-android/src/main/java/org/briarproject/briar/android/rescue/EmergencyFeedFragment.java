package org.briarproject.briar.android.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.briarproject.briar.R;
import org.briarproject.briar.android.fragment.BaseFragment;
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
}
