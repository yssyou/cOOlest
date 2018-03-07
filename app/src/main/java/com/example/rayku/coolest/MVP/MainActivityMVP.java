package com.example.rayku.coolest.MVP;

import java.util.ArrayList;

public interface MainActivityMVP {

    interface View{

        ArrayList<Song> getSongsList();
        Song getCurrentSong();
        Boolean getPlayingState();
        Boolean getLoopingState();
        int getDuration();
        int getPosition();
        void play();
        void pause();
        void playPrev();
        void playNext();
        void loop();
        void setPos(int position);
        ArrayList<String> getListsTitles();
        void setList(String title);

    }

    interface Presenter{

        void setView(MainActivityMVP.View view);

        void playSong(int index);
        void play();
        void pause();
        void playPrev();
        void playNext();
        void setLoop();
        void setRand();
        void setPos(int position);

    }

    // for the model i will implement a SQLdatabase

}
