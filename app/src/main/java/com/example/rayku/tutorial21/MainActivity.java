package com.example.rayku.tutorial21;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements SongFragment.OnFragmentInteractionListener,
ListFragment.OnFragmentInteractionListener{

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private int mCurrentState;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat.TransportControls tpControls;

    ArrayList<Song> arrayList = new ArrayList<>();

    int currentIndex;

    ArrayBlockingQueue<Runnable> queue;
    ThreadPoolExecutor mThreadPoolExecutor;
    ColorTask4 colorTask4;

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                MediaControllerCompat mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);

                MediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);

                tpControls = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();
                //tpControls.playFromMediaId(String.valueOf(R.raw.warner_tautz_off_broadway), null);

            } catch( RemoteException e ) { e.printStackTrace(); }
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if( state == null ) {
                return;
            }
            switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    break;
                }

            }
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);

            if(event.equals("playPrevSong")){
                playPrev(null);
            }

            if(event.equals("playNextSong")){
                playNext(null);
            }

            if(event.equals("lostAudioFocus")){
                playPause(null);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        queue = new ArrayBlockingQueue<>(6);
        mThreadPoolExecutor = new ThreadPoolExecutor(6, 6, 5000, TimeUnit.SECONDS, queue);

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();
    }

    @Override
    public ArrayList<Song> getSongList() {
        arrayList = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {

            int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int id = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int mimeType = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {

                String currentTitle = cursor.getString(title);
                String currentArtist = cursor.getString(artist);
                long currentId = cursor.getLong(id);
                String currentMimeType = cursor.getString(mimeType);
                int currentDuration = (int)cursor.getLong(duration);

                if (currentMimeType.equals("audio/mpeg")) {
                    arrayList.add(0, new Song(currentId, currentTitle, currentArtist, currentDuration));
                }
            } while (cursor.moveToNext());
        }

        return arrayList;
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() { }

        public static Fragment newInstance(int sectionNumber) {

            Fragment fragment = null;
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);

            switch (sectionNumber){
                case 1:
                    fragment = new ListFragment();
                    break;
                case 2:
                    fragment = new SongFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    private ListFragment listFragment;
    private SongFragment songFragment;

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() { return 2; }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            switch(position){
                case 0:
                    listFragment = (ListFragment) createdFragment;
                    break;
                case 1:
                    songFragment = (SongFragment) createdFragment;
                    break;
            }
            return createdFragment;

        }
    }

    @Override
    public void playSong(int i){
        Song currentSong = arrayList.get(i);
        long currentId = currentSong.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentId);
        tpControls.playFromUri(trackUri, null);
        tpControls.play();
        currentIndex = i;

        if(songFragment != null){
            songFragment.refreshSeekBarTask(currentSong.getDuration(), 0);
        }

    }

    @Override
    public void changeFromSeekBar(int i) {
        tpControls.seekTo(i);
    }

    @Override
    public int getCurrentState(){
        return mCurrentState;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return mThreadPoolExecutor;
    }

    @Override
    public void playPause(View view){
        if( mCurrentState == STATE_PAUSED ) {
            mCurrentState = STATE_PLAYING;
            tpControls.play();
            if(songFragment != null){
                songFragment.refreshSeekBarTask(arrayList.get(currentIndex).getDuration(), -1);
            }
        } else {
            mCurrentState = STATE_PAUSED;
            tpControls.pause();
        }
    }

    @Override
    public void playPrev(View view){
        if(currentIndex <1) currentIndex = arrayList.size();
        playSong(--currentIndex);
    }

    @Override
    public void playNext(View view){
        if(currentIndex ==arrayList.size()-1) currentIndex = -1;
        playSong(++currentIndex);
    }

    public void animateTextToLeft(final TextView textView, int translation, int duration){

        // got this time through a basic Rule of Three
        int startAnimTime = (translation-20)*duration/translation;

        TranslateAnimation startAnim = new TranslateAnimation(20, -translation, 0, 0);
        startAnim.setRepeatCount(0);
        startAnim.setStartOffset(1000);
        startAnim.setInterpolator(new LinearInterpolator());
        startAnim.setDuration(startAnimTime);
        textView.setAnimation(startAnim);

        final TranslateAnimation endlessAnim = new TranslateAnimation(translation, -translation, 0, 0);
        endlessAnim.setRepeatCount(Animation.INFINITE);
        endlessAnim.setInterpolator(new LinearInterpolator());
        endlessAnim.setDuration(duration);

        startAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setAnimation(endlessAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //colorTask4.killLightColorTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refreshColorTask();
    }

    public void refreshColorTask(){
        if(colorTask4 !=null){ colorTask4.killLightColorTask(); }
        colorTask4 = new ColorTask4(mThreadPoolExecutor, 3000, 5001,
                findViewById(R.id.bg1), findViewById(R.id.bg2), findViewById(R.id.bg3), findViewById(R.id.bg4));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
        }

        mMediaBrowserCompat.disconnect();

        mThreadPoolExecutor.shutdown();
    }


}
