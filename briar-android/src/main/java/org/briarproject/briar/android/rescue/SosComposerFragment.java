package org.briarproject.briar.android.rescue;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.briarproject.briar.R;
import org.briarproject.briar.android.BriarApplication;
import org.briarproject.briar.android.fragment.BaseFragment;
import org.briarproject.briar.android.rescue.transport.EmergencySubmissionController;
import org.rescuemesh.api.emergency.EmergencyKind;
import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.api.emergency.PrioritySuggestion;
import org.rescuemesh.core.emergency.RuleBasedPriorityClassifier;

import javax.annotation.Nullable;

/**
 * Collects a SOS/update and queues a validated envelope through a trusted
 * Briar emergency forum. Location capture is intentionally optional and is
 * added only when a fresh, permissioned location can be attached safely.
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
		EditText message = view.findViewById(R.id.rescue_message);
		message.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				renderSuggestion(view, s.toString());
			}
			@Override public void afterTextChanged(Editable s) {}
		});

		view.findViewById(R.id.rescue_store_sos_button).setOnClickListener(v ->
				storeDraft(view));
	}

	private void renderSuggestion(View view, String text) {
		PrioritySuggestion suggestion = new RuleBasedPriorityClassifier().classify(text);
		TextView label = view.findViewById(R.id.rescue_priority_suggestion);
		String indicators = suggestion.getIndicators().isEmpty() ?
				getString(R.string.rescue_no_indicators) : suggestion.getIndicators().toString();
		label.setText(getString(R.string.rescue_priority_suggestion_format,
				suggestion.getPriority().name(), indicators));
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
		// Location capture is wired separately. The user's checkbox is never
		// treated as a coordinate until permission and a fresh location exist.
		EmergencyKind kind = requireArguments().getBoolean(ARG_IS_SOS, true)
				? EmergencyKind.SOS : EmergencyKind.UPDATE;
		view.findViewById(R.id.rescue_store_sos_button).setEnabled(false);
		BriarApplication application = (BriarApplication) requireActivity()
				.getApplication();
		EmergencySubmissionController controller = new EmergencySubmissionController(
				requireContext(), application.getApplicationComponent().databaseExecutor(),
				application.getApplicationComponent().cryptoExecutor(),
				application.getApplicationComponent().identityManager(),
				application.getApplicationComponent().forumManager(),
				application.getApplicationComponent().clock(), EmergencyRuntime.getQueue());
		controller.submit(kind, priority, message, victimCount, null,
				new EmergencySubmissionController.SubmissionCallback() {
				@Override
				public void onStored(org.rescuemesh.api.emergency.EmergencyEnvelope envelope,
						boolean forumWasCreated) {
					runOnUiThreadUnlessDestroyed(() -> {
						Toast.makeText(requireContext(), R.string.rescue_message_queued,
								Toast.LENGTH_LONG).show();
						requireActivity().onBackPressed();
					});
				}
				@Override
				public void onFailure(Exception exception) {
					runOnUiThreadUnlessDestroyed(() -> {
						View button = view.findViewById(R.id.rescue_store_sos_button);
						button.setEnabled(true);
						Toast.makeText(requireContext(), R.string.rescue_message_store_failed,
								Toast.LENGTH_LONG).show();
					});
				}
			});
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
