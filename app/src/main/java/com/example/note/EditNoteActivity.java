package com.example.note;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private TextView tvCategoryName, tvReminderTime;
    private View layoutCategory, layoutReminder, layoutPin;
    private SwitchCompat switchPin, switchReminder;
    private FirebaseHelper firebaseHelper;
    
    private String noteId = null;
    private String selectedCategory = "Công việc";
    private String selectedColor = "#FFFFFF"; 
    private Date reminderDate = null;
    private boolean isChecklist = false;
    private boolean isSavingManual = false; 
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        firebaseHelper = new FirebaseHelper();
        
        // Ánh xạ
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        tvCategoryName = findViewById(R.id.tv_category_name);
        tvReminderTime = findViewById(R.id.tv_reminder_time);
        layoutCategory = findViewById(R.id.layout_category);
        layoutReminder = findViewById(R.id.layout_reminder);
        layoutPin = findViewById(R.id.layout_pin);
        switchPin = findViewById(R.id.switch_pin);
        switchReminder = findViewById(R.id.switch_reminder);
        
        ImageView ivBack = findViewById(R.id.iv_back);
        ImageView ivColorPicker = findViewById(R.id.iv_color_picker);
        ImageView ivSave = findViewById(R.id.iv_save);
        ImageView ivDelete = findViewById(R.id.iv_delete);

        ivBack.setOnClickListener(v -> finish());
        
        // NẠP DỮ LIỆU CŨ: Khôi phục trạng thái tuyệt đối
        if (getIntent().hasExtra("noteId")) {
            noteId = getIntent().getStringExtra("noteId");
            etTitle.setText(getIntent().getStringExtra("title"));
            etContent.setText(getIntent().getStringExtra("content"));
            selectedCategory = getIntent().getStringExtra("category");
            selectedColor = getIntent().getStringExtra("color");
            isChecklist = getIntent().getBooleanExtra("checklist", false);
            
            boolean pinned = getIntent().getBooleanExtra("pinned", false);
            switchPin.setChecked(pinned);

            if (getIntent().hasExtra("reminderTime")) {
                long timeMillis = getIntent().getLongExtra("reminderTime", 0);
                if (timeMillis > 0) {
                    reminderDate = new Date(timeMillis); // Nạp lại vào biến toàn cục
                    tvReminderTime.setText(sdf.format(reminderDate));
                    switchReminder.setChecked(true);
                }
            }
            
            if (selectedCategory != null) tvCategoryName.setText(selectedCategory);
            if (selectedColor != null && !selectedColor.isEmpty()) {
                getWindow().getDecorView().setBackgroundColor(Color.parseColor(selectedColor));
            }
            ivDelete.setVisibility(View.VISIBLE);
        } else {
            noteId = firebaseHelper.generateNoteId();
            ivDelete.setVisibility(View.GONE);
        }

        // Logic nhấn cả hàng (Mockup)
        layoutCategory.setOnClickListener(v -> showCategoryDialog());
        layoutPin.setOnClickListener(v -> switchPin.setChecked(!switchPin.isChecked()));
        
        layoutReminder.setOnClickListener(v -> {
            if (!switchReminder.isChecked()) {
                showDateTimePicker();
            } else {
                switchReminder.setChecked(false);
                reminderDate = null;
                tvReminderTime.setText("Chưa đặt");
                saveNoteToFirebase(); // Cập nhật ngay khi tắt
            }
        });

        ivColorPicker.setOnClickListener(v -> showColorPickerDialog());
        
        ivSave.setOnClickListener(v -> {
            isSavingManual = true;
            saveNoteToFirebase();
            finish();
        });

        ivDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Xóa ghi chú")
                .setMessage("Bạn chắc chắn chứ?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    isSavingManual = true;
                    firebaseHelper.deleteNote(noteId);
                    cancelNotification();
                    finish();
                })
                .setNegativeButton("Hủy", null).show();
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (reminderDate != null) calendar.setTime(reminderDate);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                
                reminderDate = calendar.getTime(); 
                tvReminderTime.setText(sdf.format(reminderDate));
                switchReminder.setChecked(true);
                
                // Lưu "nóng" để tránh lỗi trôi dữ liệu khi thoát
                saveNoteToFirebase(); 
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isSavingManual) {
            saveNoteToFirebase();
        }
    }

    private void saveNoteToFirebase() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) return;

        Note note = new Note();
        note.setNoteId(noteId);
        note.setOwnerId(firebaseHelper.getCurrentUserId());
        note.setTitle(title);
        note.setContent(content);
        note.setCategory(selectedCategory);
        note.setColor(selectedColor);
        note.setPinned(switchPin.isChecked());
        note.setChecklist(isChecklist);
        note.setTimestamp(new Date());

        if (switchReminder.isChecked() && reminderDate != null) {
            note.setReminderTime(reminderDate);
            scheduleNotification(title, content, reminderDate);
        } else {
            note.setReminderTime(null);
            cancelNotification();
        }

        firebaseHelper.saveNote(note);
    }

    private void scheduleNotification(String title, String content, Date date) {
        if (date.getTime() <= System.currentTimeMillis()) return;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                return;
            }
        }
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("noteIdHash", noteId.hashCode());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, noteId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
        }
    }

    private void cancelNotification() {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, noteId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.cancel(pendingIntent);
    }

    private void showCategoryDialog() {
        String[] categories = {"Công việc", "Học tập", "Cá nhân", "Ý tưởng"};
        new AlertDialog.Builder(this).setTitle("Chọn danh mục").setItems(categories, (dialog, which) -> {
            selectedCategory = categories[which];
            tvCategoryName.setText(selectedCategory);
        }).show();
    }

    private void showColorPickerDialog() {
        String[] colors = {"#FFF9C4", "#E8F5E9", "#F3E5F5", "#E3F2FD", "#FCE4EC", "#FFFFFF"};
        String[] colorNames = {"Vàng", "Xanh lá", "Tím", "Xanh dương", "Hồng", "Trắng"};
        new AlertDialog.Builder(this).setTitle("Chọn màu").setItems(colorNames, (dialog, which) -> {
            selectedColor = colors[which];
            getWindow().getDecorView().setBackgroundColor(Color.parseColor(selectedColor));
        }).show();
    }
}
