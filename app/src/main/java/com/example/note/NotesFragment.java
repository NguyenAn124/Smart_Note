package com.example.note;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesFragment extends Fragment {

    private RecyclerView rvNotes;
    private NoteAdapter adapter;
    private FirebaseHelper firebaseHelper;
    private ProgressBar pbLoading;
    private ImageView ivSearch, ivBackSearch, ivFilterDate;
    private LinearLayout layoutSearchBar;
    private EditText etSearchNotes;
    private ChipGroup chipGroupCategories;
    private View filterScroll;
    private TextView tvTitleMain;

    private List<Note> allNotes = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private String selectedCategory = "Tất cả";
    private Date selectedDate = null;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        rvNotes = view.findViewById(R.id.rv_notes);
        pbLoading = view.findViewById(R.id.pb_loading);
        ivSearch = view.findViewById(R.id.iv_search);
        ivBackSearch = view.findViewById(R.id.iv_back_search);
        ivFilterDate = view.findViewById(R.id.iv_filter_date);
        layoutSearchBar = view.findViewById(R.id.layout_search_bar);
        etSearchNotes = view.findViewById(R.id.et_search_notes);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        filterScroll = view.findViewById(R.id.filter_scroll);
        tvTitleMain = view.findViewById(R.id.tv_title_main);
        
        firebaseHelper = new FirebaseHelper();
        adapter = new NoteAdapter();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setAdapter(adapter);

        setupSearchUI();
        loadNotes();
        loadCategories();

        return view;
    }

    private void setupSearchUI() {
        ivSearch.setOnClickListener(v -> {
            layoutSearchBar.setVisibility(View.VISIBLE);
            filterScroll.setVisibility(View.VISIBLE);
            tvTitleMain.setVisibility(View.GONE);
            ivSearch.setVisibility(View.GONE);
        });

        ivBackSearch.setOnClickListener(v -> {
            layoutSearchBar.setVisibility(View.GONE);
            filterScroll.setVisibility(View.GONE);
            tvTitleMain.setVisibility(View.VISIBLE);
            ivSearch.setVisibility(View.VISIBLE);
            resetFilters();
        });

        etSearchNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivFilterDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedDate != null) calendar.setTime(selectedDate);
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                selectedDate = calendar.getTime();
                applyFilters();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void resetFilters() {
        searchQuery = "";
        selectedCategory = "Tất cả";
        selectedDate = null;
        etSearchNotes.setText("");
        if (chipGroupCategories.getChildCount() > 0) {
            ((Chip) chipGroupCategories.getChildAt(0)).setChecked(true);
        }
        applyFilters();
    }

    private void loadCategories() {
        if (firebaseHelper.getCategoriesQuery() == null) return;
        firebaseHelper.getCategoriesQuery().addSnapshotListener((value, error) -> {
            if (value != null && isAdded()) {
                categories.clear();
                chipGroupCategories.removeAllViews();
                
                // Add "All" chip
                addCategoryChip("Tất cả");

                for (DocumentSnapshot doc : value.getDocuments()) {
                    Category cat = doc.toObject(Category.class);
                    if (cat != null) {
                        categories.add(cat);
                        addCategoryChip(cat.getName());
                    }
                }
            }
        });
    }

    private void addCategoryChip(String name) {
        Chip chip = new Chip(getContext());
        chip.setText(name);
        chip.setCheckable(true);
        if (name.equals("Tất cả")) chip.setChecked(true);
        
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategory = name;
                applyFilters();
            }
        });
        chipGroupCategories.addView(chip);
    }

    private void applyFilters() {
        List<Note> filteredPinned = new ArrayList<>();
        List<Note> filteredOthers = new ArrayList<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String selectedDateStr = selectedDate != null ? sdf.format(selectedDate) : null;

        for (Note note : allNotes) {
            boolean matchesSearch = note.getTitle().toLowerCase().contains(searchQuery) || 
                                    note.getContent().toLowerCase().contains(searchQuery);
            boolean matchesCategory = selectedCategory.equals("Tất cả") || 
                                      (note.getCategory() != null && note.getCategory().equals(selectedCategory));
            boolean matchesDate = true;
            if (selectedDateStr != null && note.getTimestamp() != null) {
                matchesDate = sdf.format(note.getTimestamp()).equals(selectedDateStr);
            }

            if (matchesSearch && matchesCategory && matchesDate) {
                if (note.isPinned()) {
                    filteredPinned.add(note);
                } else {
                    filteredOthers.add(note);
                }
            }
        }
        adapter.setData(filteredPinned, filteredOthers);
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
                allNotes.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Note note = doc.toObject(Note.class);
                    if (note != null) {
                        note.setNoteId(doc.getId());
                        allNotes.add(note);
                    }
                }
                applyFilters();
            }
        });
    }
}
