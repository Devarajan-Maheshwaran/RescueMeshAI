package org.briarproject.briar.android.rescue;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.briarproject.briar.R;
import org.rescuemesh.api.emergency.EmergencyDeliveryState;
import org.rescuemesh.api.emergency.EmergencyKind;
import org.rescuemesh.api.emergency.EmergencyPriority;
import org.rescuemesh.core.emergency.EmergencyQueueItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** Card renderer for the local priority-sorted RescueMesh feed. */
public class EmergencyFeedAdapter
		extends RecyclerView.Adapter<EmergencyFeedAdapter.ViewHolder> {

	private final List<EmergencyQueueItem> items = new ArrayList<>();
	private final ItemListener listener;

	public EmergencyFeedAdapter(ItemListener listener) {
		this.listener = listener;
	}

	public void replaceItems(List<EmergencyQueueItem> updated) {
		items.clear();
		items.addAll(updated);
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_rescue_emergency, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		EmergencyQueueItem item = items.get(position);
		EmergencyPriority priority = item.getEnvelope().getPriority();
		holder.priority.setText(priority.name());
		holder.priority.setBackgroundColor(priorityColor(priority));
		holder.status.setText(item.getState().name());
		holder.message.setText(item.getEnvelope().getText());
		String time = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT).format(new Date(item.getReceivedAt()));
		Integer count = item.getEnvelope().getVictimCount();
		holder.metadata.setText(holder.itemView.getContext().getString(
				R.string.rescue_item_metadata_format, time,
				count == null ? "—" : String.valueOf(count),
				item.getSynchronisedPeerCount()));
		boolean acknowledgeable = (item.getEnvelope().getKind() == EmergencyKind.SOS
					|| item.getEnvelope().getKind() == EmergencyKind.UPDATE)
				&& item.getState() != EmergencyDeliveryState.ACKNOWLEDGED
				&& item.getState() != EmergencyDeliveryState.EXPIRED;
		holder.acknowledge.setVisibility(acknowledgeable ? View.VISIBLE : View.GONE);
		holder.acknowledge.setOnClickListener(v -> listener.onAcknowledge(item));
	}

	@Override
	public int getItemCount() { return items.size(); }

	private int priorityColor(EmergencyPriority priority) {
		if (priority == EmergencyPriority.CRITICAL) return Color.rgb(179, 38, 30);
		if (priority == EmergencyPriority.HIGH) return Color.rgb(183, 91, 0);
		return Color.rgb(27, 94, 32);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		final TextView priority, status, message, metadata;
		final View acknowledge;
		ViewHolder(View view) {
			super(view);
			priority = view.findViewById(R.id.rescue_item_priority);
			status = view.findViewById(R.id.rescue_item_status);
			message = view.findViewById(R.id.rescue_item_message);
			metadata = view.findViewById(R.id.rescue_item_metadata);
			acknowledge = view.findViewById(R.id.rescue_item_acknowledge);
		}
	}

	interface ItemListener {
		void onAcknowledge(EmergencyQueueItem item);
	}
}
