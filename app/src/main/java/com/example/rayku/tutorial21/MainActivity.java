package com.example.rayku.tutorial21;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SettingsFragment.OnFragmentInteractionListener,
ListFragment.OnFragmentInteractionListener, SongFragment.OnFragmentInteractionListener,
MyListsFragment.OnFragmentInteractionListener {

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;
    private static final int NOT_LOOPING = 0;
    private static final int LOOPING = 1;
    private static final int NOT_RAND = 0;
    private static final int RAND = 1;

    private int mCurrentState, mCurrentLoop, mCurrentRand;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat.TransportControls tpControls;

    ArrayList<Song> arrayList;
    HashMap<String, ArrayList<Integer>> lists;

    static Song currentSong;
    int currentIndex;
    long playTime, prevTime;

    ArrayBlockingQueue<Runnable> queue;
    ThreadPoolExecutor mThreadPoolExecutor;
    ColorTask4 colorTask4;

    private SettingsFragment settingsFragment;
    private ListFragment listFragment;
    private SongFragment songFragment;
    private MyListsFragment myListsFragment;

    private TabLayout mTabLayout;

    private static final int PERMISSION_REQUEST_CODE = 1;

    public View bg1, bg2, bg3, bg4;

    private LinearLayout mainLayout, newListLayout;

    Typeface typeFace;
    SharedPreferences sharedPreferences;

    Button createButton;
    EditText newName;

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                MediaControllerCompat mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);

                MediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);
                tpControls = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();
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
            switch (sectionNumber){
                case 1: return new SettingsFragment();
                case 2: return new ListFragment();
                case 3: return new SongFragment();
                case 4: return new MyListsFragment();
            }
            return null;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int position) { return PlaceholderFragment.newInstance(position + 1); }

        @Override
        public int getCount() { return 4; }

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
                case 3:
                    myListsFragment = (MyListsFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return SettingsFragment.TITLE;
                case 1: return ListFragment.TITLE;
                case 2: return SongFragment.TITLE;
                case 3: return MyListsFragment.TITLE;
            }
            return super.getPageTitle(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.mainLayout);
        newListLayout = findViewById(R.id.newListLayout);
        newListLayout.setVisibility(View.INVISIBLE);

        typeFace = Typeface.createFromAsset(getAssets(), "Amatic-Bold.ttf");

        bg1 = findViewById(R.id.bg1);
        bg2 = findViewById(R.id.bg2);
        bg3 = findViewById(R.id.bg3);
        bg4 = findViewById(R.id.bg4);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.");
            } else {
                requestPermission();
            }
        }

        sharedPreferences = this.getSharedPreferences("com.example.rayku.tutorial21", Context.MODE_PRIVATE);
        Log.i("theme", Integer.toString(sharedPreferences.getInt("theme", 666)));

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        mTabLayout = findViewById(R.id.tab);
        mTabLayout.setupWithViewPager(mViewPager);
        customizeTabLayout();

        queue = new ArrayBlockingQueue<>(6);
        mThreadPoolExecutor = new ThreadPoolExecutor(3, 3, 5000, TimeUnit.SECONDS, queue);

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();

        retrieveSongList();
        setUpListsView();
    }

    private void customizeTabLayout() {
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

    private void setUpListsView(){

        lists = new HashMap<>();

        SongsListAdapter adapter = new SongsListAdapter(this, arrayList, typeFace);
        ListView listView = findViewById(R.id.listToNew);
        listView.setAdapter(adapter);

        ((TextView)findViewById(R.id.textView)).setTypeface(typeFace);

        createButton = findViewById(R.id.createButton);
        newName = findViewById(R.id.newName);
        createButton.setTypeface(typeFace);
        newName.setTypeface(typeFace);

        lists.put("+", new ArrayList<Integer>());
        try{
            SQLiteDatabase db = this.openOrCreateDatabase("Lists", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS lists (name VARCHAR, idx INT(3))");

            db.execSQL("INSERT INTO lists (name, idx) VALUES ('kpop', 5)");
            db.execSQL("INSERT INTO lists (name, idx) VALUES ('kpop', 6)");

            db.execSQL("INSERT INTO lists (name, idx) VALUES ('jazz', 7)");
            db.execSQL("INSERT INTO lists (name, idx) VALUES ('jazz', 8)");

            db.execSQL("INSERT INTO lists (name, idx) VALUES ('latin', 12)");
            db.execSQL("INSERT INTO lists (name, idx) VALUES ('latin', 13)");
            db.execSQL("INSERT INTO lists (name, idx) VALUES ('latin', 14)");
            db.execSQL("INSERT INTO lists (name, idx) VALUES ('latin', 15)");

            Cursor c = db.rawQuery("SELECT * FROM lists", null);
            int nameIndex = c.getColumnIndex("name");
            int idxIndex = c.getColumnIndex("idx");
            c.moveToFirst();
            while(c!=null){
                String listsName = c.getString(nameIndex);
                if(!lists.keySet().contains(listsName)){
                    lists.put(listsName, new ArrayList<Integer>());
                }
                c.moveToNext();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void createNewList(View view){
        String nameInput = newName.getText().toString();

        lists.put(nameInput, new ArrayList<Integer>());
        newListLayout.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);

        if(myListsFragment != null) {
            myListsFragment.updateInterface();
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

    public void playPrev(View view){
        if(mCurrentRand == RAND) currentIndex = (int)(Math.random()*arrayList.size());
        else if(currentIndex < 1) currentIndex = arrayList.size()-1;
        else --currentIndex;
        playSong(currentIndex);
    }

    public void playNext(View view){
        if(mCurrentRand == RAND) currentIndex = (int)(Math.random()*arrayList.size());
        else if(currentIndex == arrayList.size()-1) currentIndex = 0;
        else ++currentIndex;
        playSong(currentIndex);
    }

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

    public void rand(){
        if(mCurrentRand==RAND){
            if(songFragment != null) {
                songFragment.updateRandOnOut();
            }
            mCurrentRand = NOT_RAND;
        }else{
            if(songFragment != null) {
                songFragment.updateRandOnIn();
            }
            mCurrentRand = RAND;
        }
    }

    public void setTimeOnSeekBarChange(int i){ playTime = i; }

    public ArrayList<Song> getSongList() { return arrayList; }

    public HashMap<String, ArrayList<Integer>> getLists(){ return lists; }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return mThreadPoolExecutor;
    }

    public void changeFromSeekBar(int i) {
        tpControls.seekTo(i);
    }

    public Typeface getTypeface(){ return typeFace; }
    public Song getCurrentSong(){ return currentSong; }
    public int getCurrentState(){ return mCurrentState; }
    public long getCurrentTime() {
        if(mCurrentState == 1) return playTime+System.currentTimeMillis()-prevTime;
        else return playTime;
    }
    public int getCurrentLoop(){ return mCurrentLoop; }
    public int getCurrentRand(){ return mCurrentRand; }
    public int getSpTheme(){ return sharedPreferences.getInt("theme", 666); }

    @Override
    protected void onPause() {
        super.onPause();
        if(colorTask4!=null) colorTask4.killLightColorTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sharedPreferences.getInt("theme", 666)==1)
            colorTask4 = new ColorTask4(mThreadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);
    }

    public void switchTheme(int i){

        sharedPreferences.edit().putInt("theme", i).apply();

        switch(i){
            case 0:
                if(colorTask4!=null){
                    colorTask4.killLightColorTask();
                    colorTask4 = null;
                    bg1.setBackgroundColor(Color.rgb(255,255,255));
                    bg2.setBackgroundColor(Color.rgb(255,255,255));
                    bg3.setBackgroundColor(Color.rgb(255,255,255));
                    bg4.setBackgroundColor(Color.rgb(255,255,255));
                }
                break;
            case 1:
                if(colorTask4==null)
                    colorTask4 = new ColorTask4(mThreadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);
                break;
        }

    }

    public void playList(int i){
        if(i == 0){
            mainLayout.setVisibility(View.INVISIBLE);
            newListLayout.setVisibility(View.VISIBLE);
        }
        else {
            playSong(2);
        }
    }

    @Override
    public void onBackPressed() {
        if(newListLayout.getVisibility()== View.VISIBLE){
            newListLayout.setVisibility(View.INVISIBLE);
            mainLayout.setVisibility(View.VISIBLE);
        } else
            moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaBrowserCompat.disconnect();
        mThreadPoolExecutor.shutdown();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED)return true;
        else return false;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            "Permission accepted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }







}
