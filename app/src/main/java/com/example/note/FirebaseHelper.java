package com.example.note;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirebaseHelper {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference notesRef;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        notesRef = db.collection("notes");
    }

    public String generateId() {
        return notesRef.document().getId();
    }

    public String getCurrentUserId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

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
        
        // QUAN TRỌNG: Câu lệnh này cần Composite Index trên Firebase Console
        return notesRef.whereEqualTo("ownerId", userId)
                       .orderBy("pinned", Query.Direction.DESCENDING)
                       .orderBy("timestamp", Query.Direction.DESCENDING);
    }
}
