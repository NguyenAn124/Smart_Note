package com.example.note;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);

        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_notes) {
                selectedFragment = new NotesFragment();
            } else if (id == R.id.nav_reminders) {
                selectedFragment = new RemindersFragment();
            } else if (id == R.id.nav_categories) {
                selectedFragment = new SearchFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        fabAdd.setOnClickListener(v -> {
            // Open Edit Note Activity
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });
    }
}