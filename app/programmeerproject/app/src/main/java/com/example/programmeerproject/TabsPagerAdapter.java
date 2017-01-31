package com.example.programmeerproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int pos) {
        switch (pos) {
            case 0:
                return new FromTab();
            case 1:
                return new ToTab();
            default:
                return null;
        }
    }
}
