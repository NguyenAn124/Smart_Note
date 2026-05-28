package com.example.note;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> firebaseCategories = new ArrayList<>();
    private Map<String, Integer> noteCounts = new HashMap<>(); 
    private Map<String, String> displayNames = new HashMap<>(); 
    private FirebaseHelper firebaseHelper;
    private ImageView ivAddCategory, ivDrawerMenu;

    private final String[] DEFAULT_CATEGORIES = {"Công việc", "Học tập", "Cá nhân", "Ý tưởng"};
    private ListenerRegistration catListener, noteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        firebaseHelper = new FirebaseHelper();
        rvCategories = view.findViewById(R.id.rv_categories);
        ivAddCategory = view.findViewById(R.id.iv_add_category);
        ivDrawerMenu = view.findViewById(R.id.iv_drawer_menu);

        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(categoryAdapter);

        ivAddCategory.setOnClickListener(v -> showCategoryDialog(null));
        ivDrawerMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        loadData();
        return view;
    }

    private void loadData() {
        if (firebaseHelper.getCurrentUserId() == null) return;

        // Lấy danh mục chính thức từ Firestore
        catListener = firebaseHelper.getCategoriesQuery().addSnapshotListener((value, error) -> {
            if (value != null && isAdded()) {
                firebaseCategories.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Category cat = doc.toObject(Category.class);
                    if (cat != null) {
                        cat.setId(doc.getId());
                        firebaseCategories.add(cat);
                    }
                }
                combineAndDisplay();
            }
        });

        // Lấy tất cả ghi chú để đếm số lượng (Quét cả các danh mục "ẩn" trong ghi chú)
        noteListener = firebaseHelper.getNotesQuery().addSnapshotListener((value, error) -> {
            if (value != null && isAdded()) {
                noteCounts.clear();
                displayNames.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    String catName = doc.getString("category");
                    if (catName != null && !catName.isEmpty()) {
                        String trimmed = catName.trim();
                        String lower = trimmed.toLowerCase();
                        noteCounts.put(lower, noteCounts.getOrDefault(lower, 0) + 1);
                        if (!displayNames.containsKey(lower)) {
                            displayNames.put(lower, trimmed);
                        }
                    }
                }
                combineAndDisplay();
            }
        });
    }

    private void combineAndDisplay() {
        if (!isAdded()) return;

        Map<String, Category> finalMap = new HashMap<>();

        // 1. Nạp danh mục mặc định
        for (String name : DEFAULT_CATEGORIES) {
            String lower = name.toLowerCase();
            Category c = new Category("", name, "");
            c.setCount(noteCounts.getOrDefault(lower, 0));
            finalMap.put(lower, c);
        }

        // 2. Nạp danh mục người dùng đã tạo (Firebase)
        for (Category fc : firebaseCategories) {
            String lower = fc.getName().toLowerCase();
            if (finalMap.containsKey(lower)) {
                // Nếu trùng tên mặc định, ta cập nhật ID để có thể quản lý
                finalMap.get(lower).setId(fc.getId());
            } else {
                fc.setCount(noteCounts.getOrDefault(lower, 0));
                finalMap.put(lower, fc);
            }
        }

        // 3. Nạp các danh mục "vãng lai" (có trong ghi chú nhưng chưa lưu vào list danh mục)
        for (String lowerKey : noteCounts.keySet()) {
            if (!finalMap.containsKey(lowerKey)) {
                Category extra = new Category("", displayNames.get(lowerKey), "");
                extra.setCount(noteCounts.get(lowerKey));
                finalMap.put(lowerKey, extra);
            }
        }

        List<Category> finalDisplayList = new ArrayList<>(finalMap.values());
        Collections.sort(finalDisplayList, (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        categoryAdapter.updateData(finalDisplayList);
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
    public void onDestroyView() {
        super.onDestroyView();
        if (catListener != null) catListener.remove();
        if (noteListener != null) noteListener.remove();
    }

    @Override
    public void onCategoryClick(Category category) { }

    @Override
    public void onEditCategory(Category category) {
        if (category.getId() == null || category.getId().isEmpty()) {
            Toast.makeText(getContext(), "Danh mục hệ thống không thể sửa", Toast.LENGTH_SHORT).show();
            return;
        }
        showCategoryDialog(category);
    }

    @Override
    public void onDeleteCategory(Category category) {
        if (category.getId() == null || category.getId().isEmpty()) {
            Toast.makeText(getContext(), "Danh mục hệ thống không thể xóa", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa danh mục")
                .setMessage("Bạn chắc chắn muốn xóa danh mục này?")
                .setPositiveButton("Xóa", (dialog, which) -> firebaseHelper.deleteCategory(category.getId()))
                .setNegativeButton("Hủy", null).show();
    }
}
