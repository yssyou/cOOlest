package com.example.rayku.coolest.MVP;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

class SectionsPagerAdapter extends FragmentPagerAdapter {

    Fragment[] fragments;

    SectionsPagerAdapter(FragmentManager fm, Fragment[] fragments){
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment newFragment;
        switch (position){
            case 0:
                newFragment = new ListFragment();
                fragments[0] = newFragment;
                return newFragment;
            case 1:
                newFragment = new SongFragment();
                fragments[1] = newFragment;
                return newFragment;
            case 2:
                newFragment = new MyListsFragment();
                fragments[2] = newFragment;
                return newFragment;
        }
        return null;
    }

    @Override
    public int getCount() { return fragments.length; }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return fragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "PLAYLIST";
            case 1:
                return "PLAYBACK";
            case 2:
                return "MY LISTS";
        }
        return "UNKNOWN";
    }
}