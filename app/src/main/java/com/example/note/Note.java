package com.example.note;

import java.util.Date;

public class Note {
    private String noteId;
    private String ownerId;
    private String title;
    private String content;
    private String category;
    private String color;
    private Date timestamp;
    private boolean pinned;
    private boolean checklist;
    private Date reminderTime;

    public Note() {
        this.color = "#FFFFFF";
        this.category = "Công việc";
        this.pinned = false;
        this.checklist = false;
    }

    public Note(String noteId, String ownerId, String title, String content, String category, String color, Date timestamp, boolean pinned, boolean checklist, Date reminderTime) {
        this.noteId = noteId;
        this.ownerId = ownerId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.color = color;
        this.timestamp = timestamp;
        this.pinned = pinned;
        this.checklist = checklist;
        this.reminderTime = reminderTime;
    }

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public boolean isChecklist() { return checklist; }
    public void setChecklist(boolean checklist) { this.checklist = checklist; }

    public Date getReminderTime() { return reminderTime; }
    public void setReminderTime(Date reminderTime) { this.reminderTime = reminderTime; }
}
