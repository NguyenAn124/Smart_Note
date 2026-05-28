package com.example.note;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReminderPagerAdapter extends FragmentStateAdapter {

    public ReminderPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return ReminderListFragment.newInstance(ReminderListFragment.TYPE_UPCOMING);
        } else {
            return ReminderListFragment.newInstance(ReminderListFragment.TYPE_PAST);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}