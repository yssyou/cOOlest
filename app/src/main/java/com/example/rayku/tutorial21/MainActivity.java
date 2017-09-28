package com.example.rayku.tutorial21;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        SettingsFragment.OnFragmentInteractionListener,
        ListFragment.OnFragmentInteractionListener,
SongFragment.OnFragmentInteractionListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private static final int NOT_LOOPING = 0;
    private static final int LOOPING = 1;

    private int mCurrentState, mCurrentLoop;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat.TransportControls tpControls;

    ArrayList<Song> arrayList = new ArrayList<>();

    static Song currentSong;
    int currentIndex;
    long playTime, prevTime;

    ArrayBlockingQueue<Runnable> queue;
    ThreadPoolExecutor mThreadPoolExecutor;
    ColorTask4 colorTask4;

    private SettingsFragment settingsFragment;
    private ListFragment listFragment;
    private SongFragment songFragment;

    private TabLayout mTabLayout;

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
/*
                long currentId = currentSong.getId();
                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        currentId);

                tpControls.playFromUri(trackUri, null);
*/
                playSong(0); // i had to put this here and i dont know why :S
                playPause(null);
                playPause(null);

            } catch( RemoteException e ) { e.printStackTrace(); }
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) {
                return;
            }
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    if (listFragment != null) {
                        listFragment.updateBtnOnPlay();
                    }
                    if (songFragment != null) {
                        songFragment.updateBtnOnPlay();
                    }
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    if (listFragment != null) {
                        listFragment.updateBtnOnPause();
                    }
                    if (songFragment != null) {
                        songFragment.updateBtnOnPause();
                    }
                    break;
                }
            }
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
            switch (event) {
                case "playPrevSong":
                    playPrev(null);
                    break;
                case "playNextSong":
                    playNext(null);
                    break;
                case "songFinished":
                    if(mCurrentLoop==LOOPING)
                        playSong(currentIndex);
                    else
                        playNext(null);
                    break;
                case "killSeekBarTask":
                    if(songFragment != null)
                        songFragment.killSeekBarTask();
                    break;
                case "lostAudioFocus":
                    playPause(null);
                    break;
            }
        }
    };

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() { }

        public static Fragment newInstance(int sectionNumber) {

            Fragment fragment = null;

            switch (sectionNumber){
                case 1:
                    fragment = new SettingsFragment();
                    break;
                case 2:
                    fragment = new ListFragment();
                    break;
                case 3:
                    fragment = new SongFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int position) { return PlaceholderFragment.newInstance(position + 1); }

        @Override
        public int getCount() { return 3; }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            switch(position){
                case 0:
                    settingsFragment = (SettingsFragment) createdFragment;
                    break;
                case 1:
                    listFragment = (ListFragment) createdFragment;
                    break;
                case 2:
                    songFragment = (SongFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return SettingsFragment.TITLE;

                case 1:
                    return ListFragment.TITLE;

                case 2:
                    return SongFragment.TITLE;
            }

            return super.getPageTitle(position);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrieveSongList();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        mTabLayout = (TabLayout) findViewById(R.id.tab);
        mTabLayout.setupWithViewPager(mViewPager);

        queue = new ArrayBlockingQueue<>(6);
        mThreadPoolExecutor = new ThreadPoolExecutor(6, 6, 5000, TimeUnit.SECONDS, queue);

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();

        customizeTabLayout();

    }

    private void customizeTabLayout() {
        AssetManager assetManager = getAssets();
        Typeface typeFace = Typeface.createFromAsset(assetManager, "Amatic-Bold.ttf");

        ViewGroup vg = (ViewGroup) mTabLayout.getChildAt(0);
        for (int j=0; j<vg.getChildCount(); j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            for (int i = 0; i < vgTab.getChildCount(); i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeFace);
                }
            }
        }
    }

    private void retrieveSongList(){
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

        currentIndex = 0;
        currentSong = arrayList.get(currentIndex);
    }

    @Override
    public void playSong(int i){
        currentIndex = i;
        currentSong  = arrayList.get(i);
        long currentId = currentSong.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentId);

        tpControls.playFromUri(trackUri, null);
        tpControls.play();

        prevTime = System.currentTimeMillis();
        playTime = 0;

        if(listFragment != null) {
            listFragment.updateInterface(currentSong);
        }
        if(songFragment != null) {
            songFragment.updateInterface(currentSong);
            songFragment.refreshSeekBarTask(currentSong.getDuration(), 0);
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

    @Override
    public void playPause(View view){
        if( mCurrentState == STATE_PAUSED ) {
            mCurrentState = STATE_PLAYING;
            tpControls.play();

            prevTime = System.currentTimeMillis();

            if(songFragment != null){
                songFragment.refreshSeekBarTask(arrayList.get(currentIndex).getDuration(), -1);
            }
        } else {
            mCurrentState = STATE_PAUSED;
            tpControls.pause();

            playTime += System.currentTimeMillis()-prevTime; // almacena tiempo transcurrido
        }
    }

    @Override
    public void loop(){
        if(mCurrentLoop==LOOPING){
            if(songFragment != null) {
                songFragment.updateLoopOnOut();
            }
            mCurrentLoop = NOT_LOOPING;
        }else{
            if(songFragment != null) {
                songFragment.updateLoopOnIn();
            }
            mCurrentLoop = LOOPING;
        }
    }

    public void setTimeOnSeekBarChange(int i){ playTime = i; }

    @Override
    public ArrayList<Song> getSongList() { return arrayList; }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return mThreadPoolExecutor;
    }

    @Override
    public void changeFromSeekBar(int i) {
        tpControls.seekTo(i);
    }

    public Song getCurrentSong(){ return currentSong; }
    public int getCurrentState(){ return mCurrentState; }
    public long getCurrentTime() {
        if(mCurrentState == 1) return playTime += System.currentTimeMillis()-prevTime;
        else return playTime;
    }
    public int getCurrentLoop(){ return mCurrentLoop; }

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
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaBrowserCompat.disconnect();
        mThreadPoolExecutor.shutdown();
    }

}
