package com.example.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onEditCategory(Category category);
        void onDeleteCategory(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateData(List<Category> newList) {
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;
        ImageView ivMore;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvCount = itemView.findViewById(R.id.tv_category_count);
            ivMore = itemView.findViewById(R.id.iv_more);
        }

        public void bind(Category category, OnCategoryClickListener listener) {
            tvName.setText(category.getName());
            tvCount.setText(String.valueOf(category.getCount()));

            itemView.setOnClickListener(v -> listener.onCategoryClick(category));

            ivMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), ivMore);
                popup.getMenu().add("Sửa");
                popup.getMenu().add("Xóa");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Sửa")) {
                        listener.onEditCategory(category);
                    } else if (item.getTitle().equals("Xóa")) {
                        listener.onDeleteCategory(category);
                    }
                    return true;
                });
                popup.show();
            });
        }
    }
}
