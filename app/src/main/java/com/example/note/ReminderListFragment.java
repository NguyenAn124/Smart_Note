package com.example.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReminderListFragment extends Fragment {
    private static final String ARG_TYPE = "reminder_type";
    public static final int TYPE_UPCOMING = 0;
    public static final int TYPE_PAST = 1;

    private int type;
    private RecyclerView rvReminders;
    private TextView tvEmpty;
    private ReminderAdapter adapter;
    private List<Note> reminderList = new ArrayList<>();
    private FirebaseHelper firebaseHelper;

    public static ReminderListFragment newInstance(int type) {
        ReminderListFragment fragment = new ReminderListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE);
        }
        firebaseHelper = new FirebaseHelper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder_list, container, false);
        rvReminders = view.findViewById(R.id.rvReminders);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvReminders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReminderAdapter(reminderList);
        rvReminders.setAdapter(adapter);

        loadReminders();
        return view;
    }

    private void loadReminders() {
        Query query = firebaseHelper.getNotesQuery();
        if (query == null) return;

        // Lọc những note có đặt nhắc nhở
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null || value == null) return;

                reminderList.clear();
                Date now = new Date();

                for (Note note : value.toObjects(Note.class)) {
                    if (note.getReminderTime() != null) {
                        if (type == TYPE_UPCOMING && note.getReminderTime().after(now)) {
                            reminderList.add(note);
                        } else if (type == TYPE_PAST && note.getReminderTime().before(now)) {
                            reminderList.add(note);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(reminderList.isEmpty() ? View.VISIBLE : View.GONE);
                tvEmpty.setText(type == TYPE_UPCOMING ? "Không có nhắc nhở sắp tới" : "Không có nhắc nhở đã qua");
            }
        });
    }
}