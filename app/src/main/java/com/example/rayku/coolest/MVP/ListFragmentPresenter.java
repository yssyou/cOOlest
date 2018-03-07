package com.example.rayku.coolest.MVP;

import android.support.annotation.Nullable;
import android.widget.Toast;

public class ListFragmentPresenter implements MainActivityMVP.Presenter{

    @Nullable
    private MainActivityMVP.View view;
    //private MainActivityMVP.Model model;


    public ListFragmentPresenter() {
    }

    @Override
    public void setView(MainActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void playSong(int index) {
        view.play();
    }

    @Override
    public void play() {
        view.play();
    }

    @Override
    public void pause() {
        view.pause();
    }

    @Override
    public void playPrev() {

    }

    @Override
    public void playNext() {

    }

    @Override
    public void setLoop() {

    }

    @Override
    public void setRand() {

    }

    @Override
    public void setPos(int position) {

    }
}
