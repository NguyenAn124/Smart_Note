package com.example.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment {

    private RecyclerView rvNotes;
    private NoteAdapter adapter;
    private FirebaseHelper firebaseHelper;
    private ProgressBar pbLoading;
    private ImageView ivSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        rvNotes = view.findViewById(R.id.rv_notes);
        pbLoading = view.findViewById(R.id.pb_loading);
        ivSearch = view.findViewById(R.id.iv_search);
        
        firebaseHelper = new FirebaseHelper();
        adapter = new NoteAdapter();

        // Sử dụng Staggered Grid 2 cột so le
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setAdapter(adapter);

        ivSearch.setOnClickListener(v -> {
            // Chuyển sang fragment tìm kiếm/danh mục
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_categories);
            }
        });

        loadNotes();

        return view;
    }

    private void loadNotes() {
        Query query = firebaseHelper.getNotesQuery();
        if (query == null) {
            pbLoading.setVisibility(View.GONE);
            return;
        }

        query.addSnapshotListener((value, error) -> {
            if (isAdded() && value != null) {
                pbLoading.setVisibility(View.GONE);
                
                List<Note> pinnedNotes = new ArrayList<>();
                List<Note> otherNotes = new ArrayList<>();
                
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Note note = doc.toObject(Note.class);
                    if (note != null) {
                        note.setNoteId(doc.getId()); // Đảm bảo ID được set
                        if (note.isPinned()) {
                            pinnedNotes.add(note);
                        } else {
                            otherNotes.add(note);
                        }
                    }
                }
                
                // Cập nhật adapter với logic phân đoạn mới
                adapter.setData(pinnedNotes, otherNotes);
            }
        });
    }
}
