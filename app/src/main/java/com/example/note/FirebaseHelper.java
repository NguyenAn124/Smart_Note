package com.example.note;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirebaseHelper {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference notesRef;
    private CollectionReference categoriesRef;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        notesRef = db.collection("notes");
        categoriesRef = db.collection("categories");
    }

    public String generateNoteId() {
        return notesRef.document().getId();
    }

    public String generateCategoryId() {
        return categoriesRef.document().getId();
    }

    public String getCurrentUserId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    // Note operations
    public void saveNote(Note note) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        note.setOwnerId(userId);
        notesRef.document(note.getNoteId()).set(note);
    }

    public void deleteNote(String noteId) {
        notesRef.document(noteId).delete();
    }

    public Query getNotesQuery() {
        String userId = getCurrentUserId();
        if (userId == null) return null;
        // Bỏ orderBy để tránh lỗi yêu cầu Index của Firestore khi mới bắt đầu
        return notesRef.whereEqualTo("ownerId", userId);
    }

    // Category operations
    public void saveCategory(Category category) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        category.setOwnerId(userId);
        categoriesRef.document(category.getId()).set(category);
    }

    public void deleteCategory(String categoryId) {
        categoriesRef.document(categoryId).delete();
    }

    public Query getCategoriesQuery() {
        String userId = getCurrentUserId();
        if (userId == null) return null;
        // Bỏ orderBy để tránh lỗi Index
        return categoriesRef.whereEqualTo("ownerId", userId);
    }
}
