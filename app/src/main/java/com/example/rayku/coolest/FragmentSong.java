package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class FragmentSong extends Fragment implements View.OnClickListener {

    public static final String TITLE = "PLAYBACK";

    private OnFragmentInteractionListener mListener;

    private View playBtn, prevBtn, nextBtn, loopBtn, randBtn;
    private TextView songSongTitle, songSongArtist;

    private SeekBar seekBar;

    private int currLoopDraw, currLoop_fadedDraw, currNextDraw, currPauseDraw,
    currPlayDraw, currPrevDraw, currRandDraw, currRand_fadedDraw;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song, container, false);
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
        updateTheme();
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
        int getSpTheme();
    }

    public void setUpLayout() {

        playBtn = getView().findViewById(R.id.playBtn); prevBtn = getView().findViewById(R.id.prevBtn);
        nextBtn = getView().findViewById(R.id.nextBtn); loopBtn = getView().findViewById(R.id.loopBtn);
        randBtn = getView().findViewById(R.id.randBtn);

        playBtn.setOnClickListener(this); prevBtn.setOnClickListener(this); nextBtn.setOnClickListener(this);
        loopBtn.setOnClickListener(this); randBtn.setOnClickListener(this);

        songSongTitle = getView().findViewById(R.id.song_song_title);
        songSongArtist = getView().findViewById(R.id.song_song_artist);
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "Ubuntu-C.ttf");
        songSongTitle.setTypeface(typeFace);
        songSongArtist.setTypeface(typeFace);

        seekBar = getView().findViewById(R.id.seekBar);
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
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mListener.changeFromSeekBar(seekBar.getProgress());
                seekBar.setProgress(seekBar.getProgress());
            }
        });

    }

    public void updateTheme(){
        int theme = mListener.getSpTheme();

        if(theme==0 || theme==1){
            songSongTitle.setTextColor(Color.BLACK);
            songSongArtist.setTextColor(Color.BLACK);

            seekBar.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

            currLoopDraw = R.drawable.loop;
            currLoop_fadedDraw = R.drawable.loop_faded;
            currNextDraw = R.drawable.next;
            currPauseDraw = R.drawable.pause;
            currPlayDraw = R.drawable.play;
            currPrevDraw = R.drawable.prev;
            currRandDraw = R.drawable.rand;
            currRand_fadedDraw = R.drawable.rand_faded;

        } else{
            songSongTitle.setTextColor(Color.WHITE);
            songSongArtist.setTextColor(Color.WHITE);

            seekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

            currLoopDraw = R.drawable.loop_white;
            currLoop_fadedDraw = R.drawable.loop_faded_white;
            currNextDraw = R.drawable.next_white;
            currPauseDraw = R.drawable.pause_white;
            currPlayDraw = R.drawable.play_white;
            currPrevDraw = R.drawable.prev_white;
            currRandDraw = R.drawable.rand_white;
            currRand_fadedDraw = R.drawable.rand_faded_white;
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
        updateTheme();

        if (mListener.getCurrentState() == 1) playBtn.setBackgroundResource(currPauseDraw);
        else playBtn.setBackgroundResource(currPlayDraw);

        if (mListener.getCurrentLoop() == 1) loopBtn.setBackgroundResource(currLoopDraw);
        else loopBtn.setBackgroundResource(currLoop_fadedDraw);

        if (mListener.getCurrentRand() == 1) randBtn.setBackgroundResource(currRandDraw);
        else randBtn.setBackgroundResource(currRand_fadedDraw);

        prevBtn.setBackgroundResource(currPrevDraw);
        nextBtn.setBackgroundResource(currNextDraw);
    }

    public void updateBtnOnPlay(){ playBtn.setBackgroundResource(currPauseDraw); }
    public void updateBtnOnPause(){ playBtn.setBackgroundResource(currPlayDraw); }
    public void updateLoopOnIn(){ loopBtn.setBackgroundResource(currLoopDraw); }
    public void updateLoopOnOut(){ loopBtn.setBackgroundResource(currLoop_fadedDraw); }
    public void updateRandOnIn(){ randBtn.setBackgroundResource(currRandDraw); }
    public void updateRandOnOut(){ randBtn.setBackgroundResource(currRand_fadedDraw); }

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

    public void refreshSeekBar(int duration, int progress){
        seekBar.setMax(duration);
        seekBar.setProgress(progress);
    }



}
