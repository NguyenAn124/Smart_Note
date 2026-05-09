package com.example.note;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories, rvSearchResults;
    private CategoryAdapter categoryAdapter;
    private NoteAdapter noteAdapter;
    private List<Category> categoryList;
    private List<Note> allNotesList;
    private FirebaseHelper firebaseHelper;
    private ImageView ivAddCategory, ivClearSearch;
    private EditText etSearch;
    private View scrollDefault;
    private TextView tvCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        firebaseHelper = new FirebaseHelper();
        rvCategories = view.findViewById(R.id.rv_categories);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        ivAddCategory = view.findViewById(R.id.iv_add_category);
        ivClearSearch = view.findViewById(R.id.iv_clear_search);
        etSearch = view.findViewById(R.id.et_search);
        scrollDefault = view.findViewById(R.id.scroll_default);
        tvCancel = view.findViewById(R.id.tv_cancel);

        setupCategories();
        setupSearchResults();
        setupSearchLogic();

        loadCategories();
        loadAllNotesForSearch();

        return view;
    }

    private void setupCategories() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(categoryAdapter);
        ivAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void setupSearchResults() {
        allNotesList = new ArrayList<>();
        noteAdapter = new NoteAdapter();
        // Hiển thị kết quả tìm kiếm dạng lưới 2 cột giống trang chính
        rvSearchResults.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        rvSearchResults.setAdapter(noteAdapter);
    }

    private void setupSearchLogic() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                if (query.isEmpty()) {
                    showDefaultView();
                } else {
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivClearSearch.setOnClickListener(v -> etSearch.setText(""));
        
        tvCancel.setOnClickListener(v -> {
            etSearch.setText("");
            // Có thể quay lại màn hình chính nếu cần
        });
    }

    private void showDefaultView() {
        scrollDefault.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        ivClearSearch.setVisibility(View.GONE);
    }

    private void performSearch(String query) {
        scrollDefault.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        ivClearSearch.setVisibility(View.VISIBLE);

        List<Note> filteredNotes = new ArrayList<>();
        for (Note note : allNotesList) {
            if (note.getTitle().toLowerCase().contains(query) || 
                note.getContent().toLowerCase().contains(query)) {
                filteredNotes.add(note);
            }
        }
        // Hiển thị kết quả trong NoteAdapter (không phân chia ghim ở đây để đơn giản)
        noteAdapter.setData(new ArrayList<>(), filteredNotes);
    }

    private void loadCategories() {
        if (firebaseHelper.getCategoriesQuery() == null) return;
        firebaseHelper.getCategoriesQuery().addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            categoryList.clear();
            for (DocumentSnapshot doc : value.getDocuments()) {
                Category category = doc.toObject(Category.class);
                if (category != null) {
                    category.setId(doc.getId());
                    categoryList.add(category);
                }
            }
            categoryAdapter.updateData(categoryList);
        });
    }

    private void loadAllNotesForSearch() {
        if (firebaseHelper.getNotesQuery() == null) return;
        firebaseHelper.getNotesQuery().addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            allNotesList.clear();
            for (DocumentSnapshot doc : value.getDocuments()) {
                Note note = doc.toObject(Note.class);
                if (note != null) {
                    note.setNoteId(doc.getId());
                    allNotesList.add(note);
                }
            }
        });
    }

    private void showCategoryDialog(@Nullable Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(category == null ? "Thêm danh mục" : "Sửa danh mục");
        final EditText input = new EditText(getContext());
        input.setHint("Tên danh mục");
        if (category != null) input.setText(category.getName());
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                if (category == null) {
                    String id = firebaseHelper.generateCategoryId();
                    firebaseHelper.saveCategory(new Category(id, name, firebaseHelper.getCurrentUserId()));
                } else {
                    category.setName(name);
                    firebaseHelper.saveCategory(category);
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Override
    public void onCategoryClick(Category category) {
        // Tự động điền tên danh mục vào ô tìm kiếm để lọc
        etSearch.setText(category.getName());
    }

    @Override
    public void onEditCategory(Category category) { showCategoryDialog(category); }

    @Override
    public void onDeleteCategory(Category category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc chắn muốn xóa?")
                .setPositiveButton("Xóa", (dialog, which) -> firebaseHelper.deleteCategory(category.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }
}
