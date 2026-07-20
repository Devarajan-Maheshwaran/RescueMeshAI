package org.briarproject.briar.android.rescue;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.briarproject.briar.R;
import org.briarproject.briar.android.fragment.BaseFragment;
import org.briarproject.briar.android.rescue.emergency.EmergencyDraft;
import org.briarproject.briar.android.rescue.emergency.EmergencyDraftStore;
import org.briarproject.briar.android.rescue.emergency.EmergencyPriority;

import javax.annotation.Nullable;

/**
 * Collects a local Phase-1 SOS/update draft.
 *
 * <p>Saving is deliberately labelled as local storage. Network queueing,
 * authentication, location capture, encryption bindings and relay delivery
 * belong to the Phase-2 EmergencyEnvelope implementation.</p>
 */
public class SosComposerFragment extends BaseFragment {

	private static final String ARG_IS_SOS = "isSos";
	private static final int MAX_MESSAGE_LENGTH = 500;
	private static final int MAX_VICTIM_COUNT = 999;

	public static SosComposerFragment newCriticalSos() {
		SosComposerFragment fragment = new SosComposerFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_IS_SOS, true);
		fragment.setArguments(args);
		return fragment;
	}

	public static SosComposerFragment newUpdate() {
		SosComposerFragment fragment = new SosComposerFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_IS_SOS, false);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public String getUniqueTag() {
		return "org.briarproject.briar.RESCUE_SOS_COMPOSER";
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_rescue_sos_composer, container,
				false);
	}

	@Override
	public void onViewCreated(View view,
			@Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		boolean isSos = requireArguments().getBoolean(ARG_IS_SOS, true);
		requireActivity().setTitle(isSos ? R.string.rescue_sos_title
				: R.string.rescue_update_title);

		RadioGroup priority = view.findViewById(R.id.rescue_priority_group);
		priority.check(isSos ? R.id.rescue_priority_critical
				: R.id.rescue_priority_normal);

		view.findViewById(R.id.rescue_store_sos_button).setOnClickListener(v ->
				storeDraft(view));
	}

	private void storeDraft(View view) {
		EditText messageView = view.findViewById(R.id.rescue_message);
		EditText victimCountView = view.findViewById(R.id.rescue_victim_count);
		String message = messageView.getText().toString().trim();
		if (TextUtils.isEmpty(message)) {
			messageView.setError(getString(R.string.rescue_message_required));
			messageView.requestFocus();
			return;
		}
		if (message.length() > MAX_MESSAGE_LENGTH) {
			messageView.setError(getString(R.string.rescue_message_too_long,
					MAX_MESSAGE_LENGTH));
			return;
		}

		Integer victimCount = parseVictimCount(victimCountView);
		if (victimCountView.getError() != null) return;

		RadioGroup priorityGroup = view.findViewById(R.id.rescue_priority_group);
		EmergencyPriority priority = priorityFor(priorityGroup.getCheckedRadioButtonId());
		CheckBox shareLocation = view.findViewById(R.id.rescue_share_location);
		EmergencyDraft draft = EmergencyDraft.create(priority, message, victimCount,
				shareLocation.isChecked());
		new EmergencyDraftStore(requireContext()).save(draft);
		Toast.makeText(requireContext(), R.string.rescue_draft_saved,
				Toast.LENGTH_LONG).show();
		requireActivity().onBackPressed();
	}

	@Nullable
	private Integer parseVictimCount(EditText view) {
		String value = view.getText().toString().trim();
		if (TextUtils.isEmpty(value)) return null;
		try {
			int count = Integer.parseInt(value);
			if (count < 0 || count > MAX_VICTIM_COUNT) {
				view.setError(getString(R.string.rescue_victim_count_invalid,
						MAX_VICTIM_COUNT));
				return null;
			}
			view.setError(null);
			return count;
		} catch (NumberFormatException e) {
			view.setError(getString(R.string.rescue_victim_count_invalid,
					MAX_VICTIM_COUNT));
			return null;
		}
	}

	private EmergencyPriority priorityFor(int checkedId) {
		if (checkedId == R.id.rescue_priority_high) return EmergencyPriority.HIGH;
		if (checkedId == R.id.rescue_priority_normal) return EmergencyPriority.NORMAL;
		return EmergencyPriority.CRITICAL;
	}
}
