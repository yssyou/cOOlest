package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    View listPlayBtn;
    TextView playingSongTitle, playingSongArtist;

    ArrayList<Song> arrayList;

    public static final String TITLE = "PLAYLIST";

    int currPlayDraw, currPauseDraw;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("wtfnigga", "THIS HAS BEEN CREATED");

        arrayList = mListener.getSongList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpLayout();
        updateTheme(); // i am making a double call of setUpLayout here. Will improve on the Future

        updateInterface(mListener.getCurrentSong());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setUpLayout() {
        listPlayBtn = getView().findViewById(R.id.listPlayBtn);
        playingSongTitle = getView().findViewById(R.id.playing_song_title);
        playingSongArtist = getView().findViewById(R.id.playing_song_artist);

        playingSongTitle.setTypeface(mListener.getTypeface());
        playingSongArtist.setTypeface(mListener.getTypeface());

        listPlayBtn.setOnClickListener(this);

        ListView listView = getView().findViewById(R.id.listView);
        listView.setAdapter(mListener.getAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Song s = (Song)adapterView.getItemAtPosition(i);
                mListener.playSong(mListener.getIdxFromId(s.getId()));
                Log.i("WTFNIGGA", Long.toString(s.getId()));
            }
        });

    }

    public void updateInterface(Song currentSong){
        playingSongTitle.setText(currentSong.getTitle());
        playingSongArtist.setText(currentSong.getArtist());
        playingSongTitle.setSelected(true);
        if(mListener.getCurrentState()==1) updateBtnOnPlay();
        else updateBtnOnPause();

    }

    public void updateTheme(){
        setUpLayout(); // had to call it again to re-populate the listView with the new color
        int theme = mListener.getSpTheme();
        int state = mListener.getCurrentState();

        if(theme==0 || theme==1) {
            playingSongTitle.setTextColor(Color.BLACK);
            playingSongArtist.setTextColor(Color.BLACK);
            currPlayDraw = R.drawable.play;
            currPauseDraw = R.drawable.pause;
        } else{
            playingSongTitle.setTextColor(Color.WHITE);
            playingSongArtist.setTextColor(Color.WHITE);
            currPlayDraw = R.drawable.play_white;
            currPauseDraw = R.drawable.pause_white;
        }

        if(state==0) listPlayBtn.setBackgroundResource(currPlayDraw);
        else listPlayBtn.setBackgroundResource(currPauseDraw);
    }

    interface OnFragmentInteractionListener {
        ArrayList<Song> getSongList();
        void playSong(int i);
        void playPause(View view);
        Song getCurrentSong();
        int getCurrentState();
        Typeface getTypeface();
        SongsListAdapter getAdapter();
        int getIdxFromId(long id);
        int getSpTheme();
    }

    public void updateBtnOnPlay(){ listPlayBtn.setBackgroundResource(currPauseDraw); }
    public void updateBtnOnPause(){ listPlayBtn.setBackgroundResource(currPlayDraw); }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.listPlayBtn: // este encapsulamiento permite oprimir el boton MUY RÁPIDAMENTE
                mListener.playPause(null);
                break;
        }
    }


}
