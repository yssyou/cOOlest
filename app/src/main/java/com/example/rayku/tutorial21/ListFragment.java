package com.example.rayku.tutorial21;

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

    public ListFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, container, false);
        return rootView;
    }

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpLayout();
        getSongList();
    }

    private void setUpLayout() {
        rootView = getView();
        if(rootView != null) {
            listPlayBtn = rootView.findViewById(R.id.listPlayBtn);
            playingSongTitle = rootView.findViewById(R.id.playing_song_title);
            playingSongArtist = rootView.findViewById(R.id.playing_song_artist);
            Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "Amatic-Bold.ttf");
            playingSongTitle.setTypeface(typeFace);
            playingSongArtist.setTypeface(typeFace);

            listPlayBtn.setOnClickListener(this);
        }
    }

    public void getSongList() {
        ListView listView = rootView.findViewById(R.id.listView);
        SongAdapter adapter = new SongAdapter(getContext(), mListener.getSongList());
        listView.setAdapter(adapter);

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
    }

    interface OnFragmentInteractionListener {
        ArrayList<Song> getSongList();
        void playSong(int i);
        void playPause(View view);
    }

    public void updateBtnOnPlay(){ listPlayBtn.setBackgroundResource(R.drawable.pause); }
    public void updateBtnOnPause(){ listPlayBtn.setBackgroundResource(R.drawable.play); }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.listPlayBtn:
                mListener.playPause(null);
                break;
        }
    }

}