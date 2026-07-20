package org.briarproject.briar.android.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.briarproject.briar.R;
import org.briarproject.briar.android.fragment.BaseFragment;
import org.briarproject.briar.android.rescue.profile.RescueRole;
import org.briarproject.briar.android.rescue.profile.RescueRoleStore;

import javax.annotation.Nullable;

/** Allows a person to choose a local RescueMesh UI role without cloud identity. */
public class RoleSelectionFragment extends BaseFragment {

	public static RoleSelectionFragment newInstance() {
		return new RoleSelectionFragment();
	}

	@Override
	public String getUniqueTag() {
		return "org.briarproject.briar.RESCUE_ROLE_SELECTION";
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_rescue_role_selection, container,
				false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		requireActivity().setTitle(R.string.rescue_role_title);
		RadioGroup roles = view.findViewById(R.id.rescue_role_group);
		RescueRole existing = new RescueRoleStore(requireContext()).getRole();
		roles.check(idFor(existing));
		view.findViewById(R.id.rescue_save_role_button).setOnClickListener(v -> {
			RescueRole selected = roleFor(roles.getCheckedRadioButtonId());
			if (selected == null) {
				Toast.makeText(requireContext(), R.string.rescue_role_required,
						Toast.LENGTH_SHORT).show();
				return;
			}
			new RescueRoleStore(requireContext()).setRole(selected);
			Toast.makeText(requireContext(), R.string.rescue_role_saved,
						Toast.LENGTH_SHORT).show();
			requireActivity().onBackPressed();
		});
	}

	private int idFor(@Nullable RescueRole role) {
		if (role == RescueRole.RESCUER) return R.id.rescue_role_rescuer;
		if (role == RescueRole.COORDINATOR) return R.id.rescue_role_coordinator;
		return R.id.rescue_role_victim;
	}

	@Nullable
	private RescueRole roleFor(int id) {
		if (id == R.id.rescue_role_victim) return RescueRole.VICTIM;
		if (id == R.id.rescue_role_rescuer) return RescueRole.RESCUER;
		if (id == R.id.rescue_role_coordinator) return RescueRole.COORDINATOR;
		return null;
	}
}
