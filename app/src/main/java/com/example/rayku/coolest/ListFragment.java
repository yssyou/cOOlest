package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    View rootView;

    View listPlayBtn;
    TextView playingSongTitle, playingSongArtist;

    ArrayList<Song> arrayList;

    public static final String TITLE = "PLAYLIST";

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
        arrayList = mListener.getSongList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, container, false);
        return rootView;   // I am using a rootView to see if it improves performance
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpLayout();
        updateInterface(mListener.getCurrentSong());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setUpLayout() {
        listPlayBtn = rootView.findViewById(R.id.listPlayBtn);
        playingSongTitle = rootView.findViewById(R.id.playing_song_title);
        playingSongArtist = rootView.findViewById(R.id.playing_song_artist);
        playingSongTitle.setTypeface(mListener.getTypeface());
        playingSongArtist.setTypeface(mListener.getTypeface());
        listPlayBtn.setOnClickListener(this);

        ListView listView = rootView.findViewById(R.id.listView);
        listView.setAdapter(mListener.getAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.playSong(i);
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

    interface OnFragmentInteractionListener {
        ArrayList<Song> getSongList();
        void playSong(int i);
        void playPause(View view);
        Song getCurrentSong();
        int getCurrentState();
        Typeface getTypeface();
        SongsListAdapter getAdapter();
    }

    public void updateBtnOnPlay(){ listPlayBtn.setBackgroundResource(R.drawable.pause); }
    public void updateBtnOnPause(){ listPlayBtn.setBackgroundResource(R.drawable.play); }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.listPlayBtn: // este encapsulamiento permite oprimir el boton MUY RÁPIDAMENTE
                mListener.playPause(null);
                break;
        }
    }


}
