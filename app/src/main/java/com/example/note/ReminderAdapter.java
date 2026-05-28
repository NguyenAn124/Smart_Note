package com.example.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private List<Note> reminderNotes;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ReminderAdapter(List<Note> reminderNotes) {
        this.reminderNotes = reminderNotes;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Note note = reminderNotes.get(position);
        holder.tvTitle.setText(note.getTitle());
        if (note.getReminderTime() != null) {
            holder.tvTime.setText(sdf.format(note.getReminderTime()));
        }
        // Có thể set màu sắc cho viewColorStatus dựa trên note.getColor() nếu cần
    }

    @Override
    public int getItemCount() {
        return reminderNotes.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        View viewColorStatus;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvTime = itemView.findViewById(R.id.tvReminderTime);
            viewColorStatus = itemView.findViewById(R.id.viewColorStatus);
        }
    }
}