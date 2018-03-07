package com.example.rayku.coolest.MVP;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rayku.coolest.R;

import java.util.ArrayList;

import javax.inject.Inject;

public class ListFragment extends Fragment implements MainActivityMVP.View{

    @Inject
    MainActivityMVP.Presenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("OMFG", "LISTFRAGMENTCREATED");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //presenter.setView(this);
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public ArrayList<Song> getSongsList() {
        return null;
    }

    @Override
    public Song getCurrentSong() {
        return null;
    }

    @Override
    public Boolean getPlayingState() {
        return null;
    }

    @Override
    public Boolean getLoopingState() {
        return null;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public void play() {
        Toast.makeText(getContext(), "PLAY", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void pause() {
        Toast.makeText(getContext(), "PAUSE", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void playPrev() {

    }

    @Override
    public void playNext() {

    }

    @Override
    public void loop() {

    }

    @Override
    public void setPos(int position) {

    }

    @Override
    public ArrayList<String> getListsTitles() {
        return null;
    }

    @Override
    public void setList(String title) {

    }
}
