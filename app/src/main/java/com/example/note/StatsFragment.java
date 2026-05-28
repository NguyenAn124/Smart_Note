package com.example.note;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsFragment extends Fragment {

    private TextView tvTotalNotes, tvPinnedNotes, tvStartDate, tvEndDate, tvRangeCount;
    private PieChart pieChart;
    private FirebaseHelper firebaseHelper;
    private List<Note> allNotes = new ArrayList<>();
    private Date startDate, endDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        tvTotalNotes = view.findViewById(R.id.tv_total_notes);
        tvPinnedNotes = view.findViewById(R.id.tv_pinned_notes);
        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        tvRangeCount = view.findViewById(R.id.tv_range_count);
        pieChart = view.findViewById(R.id.pieChart);

        firebaseHelper = new FirebaseHelper();

        setupDefaultDates();
        setupDatePickers();
        loadData();

        return view;
    }

    private void setupDefaultDates() {
        Calendar calendar = Calendar.getInstance();
        
        // Ngày đầu tháng hiện tại
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startDate = calendar.getTime();
        
        // Ngày cuối tháng hiện tại
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        endDate = calendar.getTime();

        tvStartDate.setText(sdf.format(startDate));
        tvEndDate.setText(sdf.format(endDate));
    }

    private void setupDatePickers() {
        tvStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (startDate != null) calendar.setTime(startDate);
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                tvStartDate.setText(sdf.format(startDate));
                updateStatsByRange();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (endDate != null) calendar.setTime(endDate);
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth, 23, 59, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                endDate = calendar.getTime();
                tvEndDate.setText(sdf.format(endDate));
                updateStatsByRange();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadData() {
        if (firebaseHelper.getNotesQuery() == null) return;
        firebaseHelper.getNotesQuery().addSnapshotListener((value, error) -> {
            if (value != null && isAdded()) {
                allNotes.clear();
                int pinnedCount = 0;

                for (DocumentSnapshot doc : value.getDocuments()) {
                    Note note = doc.toObject(Note.class);
                    if (note != null) {
                        note.setNoteId(doc.getId());
                        allNotes.add(note);
                        if (note.isPinned()) pinnedCount++;
                    }
                }

                tvTotalNotes.setText(String.valueOf(allNotes.size()));
                tvPinnedNotes.setText(String.valueOf(pinnedCount));
                updateStatsByRange();
            }
        });
    }

    private void updateStatsByRange() {
        if (startDate == null || endDate == null) return;
        
        int rangeCount = 0;
        Map<String, Integer> categoryMap = new HashMap<>();

        long startMillis = startDate.getTime();
        long endMillis = endDate.getTime();

        for (Note note : allNotes) {
            if (note.getTimestamp() != null) {
                long noteTime = note.getTimestamp().getTime();
                if (noteTime >= startMillis && noteTime <= endMillis) {
                    rangeCount++;
                    String cat = note.getCategory() != null ? note.getCategory() : "Chưa phân loại";
                    categoryMap.put(cat, categoryMap.getOrDefault(cat, 0) + 1);
                }
            }
        }
        
        tvRangeCount.setText("Số ghi chú trong kỳ: " + rangeCount);
        setupPieChart(categoryMap);
    }

    private void setupPieChart(Map<String, Integer> categoryMap) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Danh mục");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}
