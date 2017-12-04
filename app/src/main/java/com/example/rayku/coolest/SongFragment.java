package com.example.rayku.coolest;

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

public class SongFragment extends Fragment implements View.OnClickListener {

    public static final String TITLE = "PLAYBACK";

    private OnFragmentInteractionListener mListener;

    View rootView, playBtn, prevBtn, nextBtn, loopBtn, randBtn;
    TextView songSongTitle, songSongArtist;

    SeekBar seekBar;

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
    }

    interface OnFragmentInteractionListener {
        void changeFromSeekBar(int i);
        void playPrev(View view);
        void playNext(View view);
        void playPause(View view);
        int getCurrentState();
        Song getCurrentSong();
        void loop();
        int getCurrentLoop();
        void rand();
        int getCurrentRand();
    }

    public void setUpLayout() {
        rootView = getView();
        if(rootView != null) {
            playBtn = rootView.findViewById(R.id.playBtn);
            prevBtn = rootView.findViewById(R.id.prevBtn);
            nextBtn = rootView.findViewById(R.id.nextBtn);
            loopBtn = rootView.findViewById(R.id.loopBtn);
            randBtn = rootView.findViewById(R.id.randBtn);
            playBtn.setOnClickListener(this);
            prevBtn.setOnClickListener(this);
            nextBtn.setOnClickListener(this);
            loopBtn.setOnClickListener(this);
            randBtn.setOnClickListener(this);

            songSongTitle = rootView.findViewById(R.id.song_song_title);
            songSongArtist = rootView.findViewById(R.id.song_song_artist);
            Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "Ubuntu-C.ttf");
            songSongTitle.setTypeface(typeFace);
            songSongArtist.setTypeface(typeFace);

            seekBar = rootView.findViewById(R.id.seekBar);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                    if(fromUser){
                        mListener.changeFromSeekBar(i);
                        seekBar.setProgress(i);
                        mListener.changeFromSeekBar(i);
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

    public void updateInterface(Song currentSong){
        songSongTitle.setText(currentSong.getTitle());
        songSongArtist.setText(currentSong.getArtist());
        songSongTitle.setSelected(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Song currentSong = mListener.getCurrentSong();
        updateInterface(currentSong);

        if (mListener.getCurrentState() == 1){
            playBtn.setBackgroundResource(R.drawable.pause);
        }else{
            playBtn.setBackgroundResource(R.drawable.play);
        }
        if (mListener.getCurrentLoop() == 1) loopBtn.setBackgroundResource(R.drawable.loop);
        else loopBtn.setBackgroundResource(R.drawable.loop_faded);

        if (mListener.getCurrentRand() == 1) randBtn.setBackgroundResource(R.drawable.rand);
        else randBtn.setBackgroundResource(R.drawable.rand_faded);
    }

    public void updateBtnOnPlay(){ playBtn.setBackgroundResource(R.drawable.pause); }
    public void updateBtnOnPause(){ playBtn.setBackgroundResource(R.drawable.play); }
    public void updateLoopOnIn(){ loopBtn.setBackgroundResource(R.drawable.loop); }
    public void updateLoopOnOut(){ loopBtn.setBackgroundResource(R.drawable.loop_faded); }
    public void updateRandOnIn(){ randBtn.setBackgroundResource(R.drawable.rand); }
    public void updateRandOnOut(){ randBtn.setBackgroundResource(R.drawable.rand_faded); }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.playBtn:
                mListener.playPause(null);
                break;
            case R.id.prevBtn:
                mListener.playPrev(null);
                break;
            case R.id.nextBtn:
                mListener.playNext(null);
                break;
            case R.id.loopBtn:
                mListener.loop();
                break;
            case R.id.randBtn:
                mListener.rand();
                break;
        }
    }

    public void refreshSeekBar(int progress, int duration){
        seekBar.setMax(duration);
        seekBar.setProgress(progress);
    }

}
