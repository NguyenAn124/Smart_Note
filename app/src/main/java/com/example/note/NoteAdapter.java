package com.example.note;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTE = 1;

    private List<Object> items = new ArrayList<>();

    public void setData(List<Note> pinnedNotes, List<Note> otherNotes) {
        items.clear();
        if (!pinnedNotes.isEmpty()) {
            items.add("Ghi chú được ghim");
            items.addAll(pinnedNotes);
        }
        if (!otherNotes.isEmpty()) {
            items.add("Tất cả ghi chú");
            items.addAll(otherNotes);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_NOTE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Cấu hình để Header và Ghi chú ghim chiếm trọn chiều ngang
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new StaggeredGridLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (getItemViewType(position) == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeader.setText((String) items.get(position));
            layoutParams.setFullSpan(true);
        } else {
            Note note = (Note) items.get(position);
            NoteViewHolder noteHolder = (NoteViewHolder) holder;
            
            // Ghi chú được ghim chiếm 1 cột to, ghi chú thường chia 2 cột nhỏ
            layoutParams.setFullSpan(note.isPinned());

            noteHolder.tvTitle.setText(note.getTitle());
            noteHolder.tvContent.setText(note.getContent());
            noteHolder.ivPin.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
            
            try {
                if (note.getColor() != null && !note.getColor().isEmpty()) {
                    noteHolder.cardView.setCardBackgroundColor(Color.parseColor(note.getColor()));
                } else {
                    noteHolder.cardView.setCardBackgroundColor(Color.WHITE);
                }
            } catch (Exception e) {
                noteHolder.cardView.setCardBackgroundColor(Color.WHITE);
            }

            if (note.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
                noteHolder.tvDate.setText(sdf.format(note.getTimestamp()));
            }

            noteHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), EditNoteActivity.class);
                intent.putExtra("noteId", note.getNoteId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("category", note.getCategory());
                intent.putExtra("color", note.getColor());
                intent.putExtra("pinned", note.isPinned());
                intent.putExtra("checklist", note.isChecklist());
                if (note.getReminderTime() != null) {
                    intent.putExtra("reminderTime", note.getReminderTime().getTime());
                }
                v.getContext().startActivity(intent);
            });
        }
        holder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(View view) {
            super(view);
            tvHeader = view.findViewById(R.id.tv_header_title);
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        ImageView ivPin;
        CardView cardView;

        NoteViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_item_title);
            tvContent = view.findViewById(R.id.tv_item_content);
            tvDate = view.findViewById(R.id.tv_item_date);
            ivPin = view.findViewById(R.id.iv_item_pin);
            cardView = view.findViewById(R.id.cv_note_item);
        }
    }
}
