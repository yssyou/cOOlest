package com.example.rayku.coolest;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

class AdapterSectionsPager extends FragmentPagerAdapter {

    FragmentList fragmentList;
    FragmentSong fragmentSong;
    FragmentMyLists fragmentMyLists;

    AdapterSectionsPager(FragmentManager fm, FragmentList fragmentList, FragmentSong fragmentSong, FragmentMyLists fragmentMyLists){
        super(fm);
        this.fragmentList = fragmentList;
        this.fragmentSong = fragmentSong;
        this.fragmentMyLists = fragmentMyLists;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new FragmentList();
            case 1: return new FragmentSong();
            case 2: return new FragmentMyLists();
        }
        return null;
    }

    @Override
    public int getCount() { return 3; }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        switch(position){
            case 0:
                fragmentList = (FragmentList) createdFragment;
                break;
            case 1:
                fragmentSong = (FragmentSong) createdFragment;
                break;
            case 2:
                fragmentMyLists = (FragmentMyLists) createdFragment;
                break;
        }
        return createdFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return FragmentList.TITLE;
            case 1: return FragmentSong.TITLE;
            case 2: return FragmentMyLists.TITLE;
        }
        return super.getPageTitle(position);
    }
}
