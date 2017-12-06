package com.example.rayku.coolest;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    private int currState, currLoop, currRand;

    private MediaBrowserCompat mediaBrowserCompat;
    private MediaControllerCompat.TransportControls tpControls;

    ArrayList<Song> songsList;
    HashMap<String, ArrayList<Long>> customLists;
    String currList = "MAIN";

    ArrayList<Long> theIDs;

    static Song currSong;
    int currIdx, auxIdx;

    ArrayBlockingQueue<Runnable> queue;
    ThreadPoolExecutor threadPoolExecutor;
    ColorTaskLight colorTaskLight;
    ColorTaskDark colorTaskDark;
    SeekBarTask seekBarTask;

    private SettingsFragment settingsFragment;
    private ListFragment listFragment;
    private SongFragment songFragment;
    private MyListsFragment myListsFragment;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private static final int PERMISSION_REQUEST_CODE = 1;

    public View bg1, bg2, bg3, bg4;

    private LinearLayout newListLayout;

    Typeface typeFace;
    SharedPreferences sharedPreferences;

    Button createButton;
    EditText newName;

    SQLiteDatabase SQLiteDB;

    SongsListAdapter adapter;

    private MediaBrowserCompat.ConnectionCallback mediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                MediaControllerCompat mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mediaControllerCompatCallback);

                MediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);
                tpControls = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();

            } catch( RemoteException e ) { e.printStackTrace(); }
        }
    };

    private MediaControllerCompat.Callback mediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) {
                return;
            }
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    currState = STATE_PLAYING;
                    if (listFragment != null) {
                        listFragment.updateBtnOnPlay();
                    }
                    if (songFragment != null) {
                        songFragment.updateBtnOnPlay();
                    }
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    currState = STATE_PAUSED;
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
                    if(currLoop ==LOOPING)
                        playSong(currIdx);
                    else
                        playNext(null);
                    break;
                case "lostAudioFocus":
                    playPause(null);
                    break;
                case "sureRefreshIt!":
                    if(songFragment!=null)
                        songFragment.refreshSeekBar(extras.getInt("position"), extras.getInt("duration"));
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

        SQLiteDB = this.openOrCreateDatabase("Lists", MODE_PRIVATE, null);
        //SQLiteDB.execSQL("DROP TABLE IF EXISTS lists");
        SQLiteDB.execSQL("CREATE TABLE IF NOT EXISTS lists (name VARCHAR, id INTEGER)");

        newListLayout = findViewById(R.id.newListLayout);
        newListLayout.setVisibility(View.INVISIBLE);

        typeFace = Typeface.createFromAsset(getAssets(), "Ubuntu-C.ttf");

        bg1 = findViewById(R.id.bg1); bg2 = findViewById(R.id.bg2); bg3 = findViewById(R.id.bg3);  bg4 = findViewById(R.id.bg4);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.");
            } else {
                requestPermission();
            }
        }

        sharedPreferences = this.getSharedPreferences("com.example.rayku.coolest", Context.MODE_PRIVATE);
        Log.i("theme", Integer.toString(sharedPreferences.getInt("theme", 666)));

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);

        tabLayout = findViewById(R.id.tab);
        tabLayout.setupWithViewPager(viewPager);
        customizeTabLayout(Color.BLACK);

        queue = new ArrayBlockingQueue<>(6);
        threadPoolExecutor = new ThreadPoolExecutor(3, 3, 5000, TimeUnit.SECONDS, queue);

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
                mediaBrowserCompatConnectionCallback, getIntent().getExtras());
        mediaBrowserCompat.connect();

        retrieveSongList();
        adapter = new SongsListAdapter(this, songsList, typeFace);
        setupListsView();

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

    }

    private void customizeTabLayout(int textColor) {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        for (int j=0; j<vg.getChildCount(); j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            for (int i = 0; i < vgTab.getChildCount(); i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeFace);
                    ((TextView) tabViewChild).setTextColor(textColor);
                }
            }
        }
        tabLayout.setSelectedTabIndicatorColor(textColor);
    }

    private void setupListsView(){
        customLists = new HashMap<>();
        customLists.put("+", new ArrayList<Long>());

        ListView listToNew = findViewById(R.id.listToNew);
        listToNew.setAdapter(adapter);

        ((TextView)findViewById(R.id.textView)).setTypeface(typeFace);

        createButton = findViewById(R.id.createButton);
        newName = findViewById(R.id.newName);
        createButton.setTypeface(typeFace);
        newName.setTypeface(typeFace);

        try{
            Cursor c = SQLiteDB.rawQuery("SELECT * FROM lists", null);
            int nameIndex = c.getColumnIndex("name");
            int idIndex = c.getColumnIndex("id");
            c.moveToFirst();
            while(c!=null){
                String listName = c.getString(nameIndex);
                long id = c.getLong(idIndex);// does this work? the getFloat?
                if(!customLists.keySet().contains(listName))
                    customLists.put(listName, new ArrayList<Long>());
                else customLists.get(listName).add(id);
                c.moveToNext();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        theIDs = new ArrayList<>();

        listToNew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Song s = (Song)adapterView.getItemAtPosition(i);

                if(view.getBackground()==null) {

                    //theIDs.add(songsList.get(i).getId());
                    theIDs.add(s.getId());


                    view.setBackgroundColor(Color.argb(60, 0, 0, 0));
                } else{
                    //theIDs.remove(songsList.get(i).getId());
                    theIDs.remove(s.getId());

                    view.setBackground(null);
                }
            }
        });

    }

    public void createNewList(View view){

        String nameInput = newName.getText().toString();

        try{
            for(long anIndex : theIDs){
                String stringToAddIndex = "INSERT INTO lists (name, id) VALUES (" + "'"+nameInput+"', "+Long.toString(anIndex)+")";
                SQLiteDB.execSQL(stringToAddIndex);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        customLists.put(nameInput, theIDs);

        newListLayout.setVisibility(View.INVISIBLE);

        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        if(myListsFragment != null) {
            myListsFragment.updateInterface();
        }

        theIDs = new ArrayList<>(); // we refresh theIDs for a new list
    }

    public void setList(String listName){
        if(listName.equals("+")){
            newListLayout.setVisibility(View.VISIBLE);

            tabLayout.setVisibility(View.INVISIBLE);
            viewPager.setVisibility(View.INVISIBLE);

        }else if(listName.equals(currList)){
            currList = "MAIN";
        } else{
            currList = listName;
        }
        auxIdx = 0;
    }

    public int getIdxFromId(long id){
        for(int i=0; i<songsList.size(); i++)
            if(songsList.get(i).getId() == id) return i;
        return 0;
    }

    private void retrieveSongList(){
        songsList = new ArrayList<>();

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
                    songsList.add(0, new Song(currentId, currentTitle, currentArtist, currentDuration));
                }
            } while (cursor.moveToNext());
        }

        currIdx = 0;
        currSong = songsList.get(currIdx);
    }

    public void playSong(int i){

        if(seekBarTask == null){
            seekBarTask = new SeekBarTask();
            seekBarTask.executeOnExecutor(threadPoolExecutor, tpControls);
        }

        currIdx = i;
        currSong = songsList.get(i);
        long currentId = currSong.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentId);

        tpControls.playFromUri(trackUri, null);
        tpControls.play();

        if(listFragment != null) {
            listFragment.updateInterface(currSong);
        }
        if(songFragment != null) {
            songFragment.updateInterface(currSong);
        }
    }

    public void playPrev(View view){

        if(currList.equals("MAIN")) {
            if (currRand == RAND) currIdx = (int) (Math.random() * songsList.size());
            else if (currIdx < 1) currIdx = songsList.size() - 1;
            else --currIdx;
        } else{
            ArrayList<Long> currList = customLists.get(this.currList);
            if(auxIdx ==0) auxIdx = currList.size()-1;
            else auxIdx--;
            currIdx = getIdxFromId(currList.get(auxIdx));
        }
        playSong(currIdx);
    }

    public void playNext(View view){

        if(currList.equals("MAIN")) {
            if (currRand == RAND) currIdx = (int) (Math.random() * songsList.size());
            else if (currIdx == songsList.size() - 1) currIdx = 0;
            else ++currIdx;
            playSong(currIdx);
        } else{
            ArrayList<Long> currList = customLists.get(this.currList);
            if(auxIdx == currList.size() - 1) auxIdx = 0;
            else auxIdx++;
            currIdx = getIdxFromId(currList.get(auxIdx));
        }
        playSong(currIdx);
    }

    public void playPause(View view){
        if( currState == STATE_PAUSED ) {
            currState = STATE_PLAYING;
            tpControls.play();
        } else {
            currState = STATE_PAUSED;
            tpControls.pause();
        }
    }

    public void loop(){
        if(currLoop ==LOOPING){
            if(songFragment != null) {
                songFragment.updateLoopOnOut();
            }
            currLoop = NOT_LOOPING;
        }else{
            if(songFragment != null) {
                songFragment.updateLoopOnIn();
            }
            currLoop = LOOPING;
        }
    }

    public void rand(){
        if(currRand ==RAND){
            if(songFragment != null) {
                songFragment.updateRandOnOut();
            }
            currRand = NOT_RAND;
        }else{
            if(songFragment != null) {
                songFragment.updateRandOnIn();
            }
            currRand = RAND;
        }
    }

    public void changeFromSeekBar(int i) {
        tpControls.seekTo(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(colorTaskLight !=null) colorTaskLight.killColorTaskLight();
        if(colorTaskDark !=null) colorTaskDark.killColorTaskDark();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sharedPreferences.getInt("theme", 666)==1) {
            colorTaskLight = new ColorTaskLight(threadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);
            customizeTabLayout(Color.BLACK);
        }
        if(sharedPreferences.getInt("theme", 666)==3) {
            colorTaskDark = new ColorTaskDark(threadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);
            customizeTabLayout(Color.WHITE);
        }
    }

    public void switchTheme(int i){

        sharedPreferences.edit().putInt("theme", i).apply();
        if(settingsFragment!=null) settingsFragment.updateTheme();
        if(songFragment!=null) songFragment.updateTheme();

        if(i==0 || i==1) customizeTabLayout(Color.BLACK);
        else customizeTabLayout(Color.WHITE);

        if(colorTaskLight!=null) {
            colorTaskLight.killColorTaskLight();
            colorTaskLight = null; }
        if(colorTaskDark!=null) {
            colorTaskDark.killColorTaskDark();
            colorTaskDark = null; }

        if(i==0){
            bg1.setBackgroundColor(Color.WHITE); bg2.setBackgroundColor(Color.WHITE);
            bg3.setBackgroundColor(Color.WHITE); bg4.setBackgroundColor(Color.WHITE);
        }
        if(i==2){
            bg1.setBackgroundColor(Color.BLACK); bg2.setBackgroundColor(Color.BLACK);
            bg3.setBackgroundColor(Color.BLACK); bg4.setBackgroundColor(Color.BLACK);
        }
        if(i==1) colorTaskLight = new ColorTaskLight(threadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);
        if(i==3) colorTaskDark = new ColorTaskDark(threadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);

    }

    @Override
    public void onBackPressed() {
        if(newListLayout.getVisibility()== View.VISIBLE){
            newListLayout.setVisibility(View.INVISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        }
        else moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
        threadPoolExecutor.shutdown();
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

    public ArrayList<Song> getSongList() { return songsList; }
    public HashMap<String, ArrayList<Long>> getCustomLists(){ return customLists; }
    public Typeface getTypeface(){ return typeFace; }
    public Song getCurrentSong(){ return currSong; }
    public int getCurrentState(){ return currState; }
    public int getCurrentLoop(){ return currLoop; }
    public int getCurrentRand(){ return currRand; }
    public int getSpTheme(){ return sharedPreferences.getInt("theme", 666); }
    public SongsListAdapter getAdapter(){ return adapter; }
    public String getCurrList(){ return currList; }

}
