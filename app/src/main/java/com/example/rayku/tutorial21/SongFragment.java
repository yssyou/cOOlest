package com.example.rayku.tutorial21;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.ThreadPoolExecutor;

public class SongFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    View rootView;

    View playBtn, prevBtn, nextBtn;
    TextView songSongTitle, songSongArtist;

    SeekBar seekBar;
    ThreadPoolExecutor threadPoolExecutor;
    SeekBarTask seekBarTask;

    public SongFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_song, container, false);
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
        threadPoolExecutor = mListener.getThreadPoolExecutor();
    }

    public void refreshSeekBarTask(int max, int progress){
        killSeekBarTask();

        seekBar.setMax(max);
        if(progress != -1) {
            seekBar.setProgress(progress);
        }
        seekBarTask = new SeekBarTask();
        seekBarTask.executeOnExecutor(threadPoolExecutor, seekBar);
    }

    public void killSeekBarTask(){
        if(seekBarTask != null) {
            seekBarTask.cancel(true);
        }
    }

    interface OnFragmentInteractionListener {
        void changeFromSeekBar(int i);
        void playPrev(View view);
        void playNext(View view);
        int getCurrentState();
        void playPause(View view);
        ThreadPoolExecutor getThreadPoolExecutor();
    }

    public void setUpLayout() {
        rootView = getView();
        if(rootView != null) {
            playBtn = rootView.findViewById(R.id.playBtn);
            prevBtn = rootView.findViewById(R.id.prevBtn);
            nextBtn = rootView.findViewById(R.id.nextBtn);
            playBtn.setOnClickListener(this);
            prevBtn.setOnClickListener(this);
            nextBtn.setOnClickListener(this);

            songSongTitle = rootView.findViewById(R.id.song_song_title);
            songSongArtist = rootView.findViewById(R.id.song_song_artist);
            Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "Amatic-Bold.ttf");
            songSongTitle.setTypeface(typeFace);
            songSongTitle.setTypeface(typeFace);

            seekBar = rootView.findViewById(R.id.seekBar);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                    if(fromUser){
                        mListener.changeFromSeekBar(i);
                        seekBar.setProgress(i);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mListener.changeFromSeekBar(seekBar.getProgress());
                    seekBar.setProgress(seekBar.getProgress());
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.playBtn:
                mListener.playPause(null);
                if( mListener.getCurrentState() == 1 ) {
                    playBtn.setBackgroundResource(R.drawable.pause);
                } else {
                    playBtn.setBackgroundResource(R.drawable.play);
                }
                break;
            case R.id.prevBtn:
                mListener.playPrev(null);
                playBtn.setBackgroundResource(R.drawable.pause);
                break;
            case R.id.nextBtn:
                mListener.playNext(null);
                playBtn.setBackgroundResource(R.drawable.pause);
                break;
        }
    }

}
