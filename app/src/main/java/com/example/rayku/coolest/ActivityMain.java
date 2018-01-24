package com.example.rayku.coolest;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActivityMain extends AppCompatActivity implements FragmentSettings.OnFragmentInteractionListener,
FragmentList.OnFragmentInteractionListener, FragmentSong.OnFragmentInteractionListener,
FragmentMyLists.OnFragmentInteractionListener, FragmentNewList.OnFragmentInteractionListener {

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

    static Song currSong;
    int currIdx, auxIdx;

    ThreadPoolExecutor threadPoolExecutor;
    TaskColorLight taskColorLight;
    TaskColorDark taskColorDark;
    SeekBarTask seekBarTask;

    private FragmentSettings fragmentSettings;
    private FragmentList fragmentList;
    private FragmentSong fragmentSong;
    private FragmentMyLists fragmentMyLists;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public View bg1, bg2, bg3, bg4;

    Typeface typeFace;
    SharedPreferences sharedPreferences;

    SQLiteDatabase SQLiteDB;

    AdapterSongsList adapter; // one for the FragmentList and other for the listToNew
    SearchView searchView;
    ImageView searchIcon;
    ImageView searchCloseIcon;

    MediaControllerCompat.Callback mediaControllerCompatCallback;

    boolean gotFilePermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bg1 = findViewById(R.id.bg1);
        bg2 = findViewById(R.id.bg2);
        bg3 = findViewById(R.id.bg3);
        bg4 = findViewById(R.id.bg4);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        searchView = findViewById(R.id.searchView);
        searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchCloseIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);

        SQLiteDB = this.openOrCreateDatabase("Lists", MODE_PRIVATE, null);
        //SQLiteDB.execSQL("DROP TABLE IF EXISTS lists");
        SQLiteDB.execSQL("CREATE TABLE IF NOT EXISTS lists (name VARCHAR, id INTEGER)");

        typeFace = Typeface.createFromAsset(getAssets(), "Ubuntu-C.ttf");

        sharedPreferences = this.getSharedPreferences("com.example.rayku.coolest", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("theme", 0).apply();

        threadPoolExecutor = new ThreadPoolExecutor(3, 3, 5000, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(6));

        retrieveSongList();

        if(songsList.size()>0) {
            setUpMediaBrowserService();
            adapter = new AdapterSongsList(getApplicationContext(), songsList, typeFace, getSpTheme());

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return false;
                }
            });

            refreshCustomLists();

            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(sectionsPagerAdapter);
            viewPager.setCurrentItem(1);
            tabLayout.setupWithViewPager(viewPager);
            customizeTabLayout(Color.BLACK);
        }


    }

    private void retrieveSongList(){
        songsList = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor c = contentResolver.query(uri, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            int title = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int id = c.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int mimeType = c.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int duration = c.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                String currTitle = c.getString(title);
                String currentArtist = c.getString(artist);
                long currId = c.getLong(id);
                String currMimeType = c.getString(mimeType);
                int currDuration = (int)c.getLong(duration);

                if (currMimeType.equals("audio/mpeg"))
                    songsList.add(0, new Song(currId, currTitle, currentArtist, currDuration));

            } while (c.moveToNext());
        }

        uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        c = contentResolver.query(uri, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            int title = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int id = c.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int mimeType = c.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int duration = c.getColumnIndex(MediaStore.Audio.Media.DURATION);
            do {
                String currTitle = c.getString(title);
                String currentArtist = c.getString(artist);
                long currId = c.getLong(id);
                String currMimeType = c.getString(mimeType);
                int currDuration = (int)c.getLong(duration);

                if (currMimeType.equals("audio/mpeg")
                        && !currTitle.equals("shutdownsound")
                        && !currTitle.equals("bootsound")) {
                    songsList.add(0, new Song(currId, currTitle, currentArtist, currDuration));
                }
            } while (c.moveToNext());
        }

        if (c!= null) c.close();

        currIdx = 0;

        if(songsList.size()!=0){
            currSong = songsList.get(currIdx);
        }


    }

    private void setUpMediaBrowserService() {

        MediaBrowserCompat.ConnectionCallback mediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

            @Override
            public void onConnected() {
                super.onConnected();
                try {
                    MediaControllerCompat mMediaControllerCompat = new MediaControllerCompat(ActivityMain.this, mediaBrowserCompat.getSessionToken());
                    mMediaControllerCompat.registerCallback(mediaControllerCompatCallback);

                    MediaControllerCompat.setMediaController(ActivityMain.this, mMediaControllerCompat);
                    tpControls = MediaControllerCompat.getMediaController(ActivityMain.this).getTransportControls();

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        mediaControllerCompatCallback = new MediaControllerCompat.Callback() {

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
                if (state == null) {
                    return;
                }
                switch (state.getState()) {
                    case PlaybackStateCompat.STATE_PLAYING: {
                        currState = STATE_PLAYING;
                        if (fragmentList != null) {
                            fragmentList.updateBtnOnPlay();
                        }
                        if (fragmentSong != null) {
                            fragmentSong.updateBtnOnPlay();
                        }
                        break;
                    }
                    case PlaybackStateCompat.STATE_PAUSED: {
                        currState = STATE_PAUSED;
                        if (fragmentList != null) {
                            fragmentList.updateBtnOnPause();
                        }
                        if (fragmentSong != null) {
                            fragmentSong.updateBtnOnPause();
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
                        if (currLoop == LOOPING)
                            playSong(currIdx);
                        else
                            playNext(null);
                        break;
                    case "lostAudioFocus":
                        playPause(null);
                        break;
                    case "sureRefreshIt!":
                        if (fragmentSong != null)
                            fragmentSong.refreshSeekBar(extras.getInt("position"), extras.getInt("duration"));
                        break;
                }
            }

        };

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
                mediaBrowserCompatConnectionCallback, getIntent().getExtras());
        mediaBrowserCompat.connect();
    }

    private void refreshCustomLists() {

        customLists = new HashMap<>();
        customLists.put("+", new ArrayList<Long>());

        try {
            Cursor c = SQLiteDB.rawQuery("SELECT * FROM lists", null);
            int nameIndex = c.getColumnIndex("name");
            int idIndex = c.getColumnIndex("id");
            c.moveToFirst();

            do {
                String listName = c.getString(nameIndex);
                long id = c.getLong(idIndex);
                if (!customLists.keySet().contains(listName))
                    customLists.put(listName, new ArrayList<Long>());
                else customLists.get(listName).add(id);
            } while (c.moveToNext());
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void customizeTabLayout(int textColor){
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

    private void customizeSearchView(int textColor){
        ((TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(textColor);
        searchIcon.setColorFilter(textColor);
        searchCloseIcon.setColorFilter(textColor);
    }

    public void createNewList(ArrayList<Long> theIDs, String listTitle){

        if(getSupportFragmentManager().findFragmentByTag("FragmentConfirm")!=null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(getSupportFragmentManager().findFragmentByTag("FragmentConfirm")).commit();
        }

        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        if(customLists.containsKey(listTitle)) return;

        try{
            for(long anIndex : theIDs){
                String stringToAddIndex = "INSERT INTO lists (name, id) VALUES (" + "'"+listTitle+"', "+Long.toString(anIndex)+")";
                SQLiteDB.execSQL(stringToAddIndex);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        customLists.put(listTitle, theIDs);

        if(fragmentMyLists != null) fragmentMyLists.updateInterface(getSpTheme());

    }

    public void deleteList(String name){

        SQLiteDB.delete("lists", "name"+"='"+name+"'", null);
        customLists.remove(name);
        if(fragmentMyLists !=null) fragmentMyLists.updateInterface(getSpTheme());
        currList = "MAIN";

    }

    public void setList(String listName){
        if(listName.equals("+")){

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.frameLayout, new FragmentNewList(),"FragmentNewList");
            transaction.addToBackStack(null);
            transaction.commit();

            tabLayout.setVisibility(View.INVISIBLE);
            viewPager.setVisibility(View.INVISIBLE);

            currList = "MAIN";

        } else {

            if (listName.equals(currList)) {
                currList = "MAIN";
            } else {
                currList = listName;
                auxIdx = 0;
            }

            if (!currList.equals("MAIN"))
                playSong(getIdxFromId(customLists.get(currList).get(0)));
        }

    }

    public int getIdxFromId(long id){
        for(int i=0; i<songsList.size(); i++)
            if(songsList.get(i).getId() == id) return i;
        return 0;
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

        Bundle extras = new Bundle();
        extras.putString("title", currSong.getTitle());
        extras.putString("artist", currSong.getArtist());

        tpControls.playFromUri(trackUri, extras);
        tpControls.play();

        if(fragmentList != null) {
            fragmentList.updateInterface(currSong);
        }
        if(fragmentSong != null) {
            fragmentSong.updateInterface(currSong);
        }
    }

    public void playPrev(View view){

        if(currList.equals("MAIN")) {
            if (currRand == RAND) currIdx = (int) (Math.random() * songsList.size());
            else if (currIdx < 1) currIdx = songsList.size() - 1;
            else --currIdx;
        } else{
            ArrayList<Long> list = customLists.get(currList);
            if(auxIdx == 0) auxIdx = list.size()-1;
            else auxIdx--;
            currIdx = getIdxFromId(list.get(auxIdx));
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
            ArrayList<Long> list = customLists.get(currList);
            if(auxIdx == list.size() - 1) auxIdx = 0;
            else auxIdx++;
            currIdx = getIdxFromId(list.get(auxIdx));
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
            if(fragmentSong != null) {
                fragmentSong.updateLoopOnOut();
            }
            currLoop = NOT_LOOPING;
        }else{
            if(fragmentSong != null) {
                fragmentSong.updateLoopOnIn();
            }
            currLoop = LOOPING;
        }
    }

    public void rand(){
        if(currRand ==RAND){
            if(fragmentSong != null) {
                fragmentSong.updateRandOnOut();
            }
            currRand = NOT_RAND;
        }else{
            if(fragmentSong != null) {
                fragmentSong.updateRandOnIn();
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
        if(taskColorLight !=null) taskColorLight.killColorTaskLight();
        if(taskColorDark !=null) taskColorDark.killColorTaskDark();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(getSpTheme()==1) {
            taskColorLight = new TaskColorLight(threadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);
        }
        if(getSpTheme()==3) {
            taskColorDark = new TaskColorDark(threadPoolExecutor, bg1, bg2, bg3, bg4);
        }

        if(gotFilePermission) switchTheme(getSpTheme());

    }

    public void switchTheme(int i){

        sharedPreferences.edit().putInt("theme", i).apply();

        adapter = new AdapterSongsList(this, songsList, typeFace, getSpTheme());

        if(fragmentSettings !=null) fragmentSettings.updateTheme();
        if(fragmentSong !=null) fragmentSong.updateTheme();
        if(fragmentList !=null) fragmentList.updateTheme();

        if(i==0 || i==1){
            customizeTabLayout(Color.BLACK);
            customizeSearchView(Color.BLACK);
        }
        else{
            customizeTabLayout(Color.WHITE);
            customizeSearchView(Color.WHITE);
        }

        if(taskColorLight !=null) {
            taskColorLight.killColorTaskLight();
            taskColorLight = null; }
        if(taskColorDark !=null) {
            taskColorDark.killColorTaskDark();
            taskColorDark = null; }

        if(i==0){
            bg1.setBackgroundColor(Color.WHITE); bg2.setBackgroundColor(Color.WHITE);
            bg3.setBackgroundColor(Color.WHITE); bg4.setBackgroundColor(Color.WHITE);
        }
        if(i==2){
            bg1.setBackgroundColor(Color.BLACK); bg2.setBackgroundColor(Color.BLACK);
            bg3.setBackgroundColor(Color.BLACK); bg4.setBackgroundColor(Color.BLACK);
        }
        if(i==1) taskColorLight = new TaskColorLight(threadPoolExecutor, 3000, 5001, bg1, bg2, bg3, bg4);
        if(i==3) taskColorDark = new TaskColorDark(threadPoolExecutor, bg1, bg2, bg3, bg4);

    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentByTag("FragmentNewList")!=null){

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(getSupportFragmentManager().findFragmentByTag("FragmentNewList")).commit();

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

    public ArrayList<Song> getSongList() { return songsList; }
    public HashMap<String, ArrayList<Long>> getCustomLists(){ return customLists; }
    public Typeface getTypeface(){ return typeFace; }
    public Song getCurrentSong(){ return currSong; }
    public int getCurrentState(){ return currState; }
    public int getCurrentLoop(){ return currLoop; }
    public int getCurrentRand(){ return currRand; }
    public int getSpTheme(){ return sharedPreferences.getInt("theme", 0); }
    public AdapterSongsList getAdapter(){ return adapter; }
    public String getCurrList(){ return currList; }

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
                    fragmentSettings = (FragmentSettings) createdFragment;
                    break;
                case 1:
                    fragmentList = (FragmentList) createdFragment;
                    break;
                case 2:
                    fragmentSong = (FragmentSong) createdFragment;
                    break;
                case 3:
                    fragmentMyLists = (FragmentMyLists) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return FragmentSettings.TITLE;
                case 1: return FragmentList.TITLE;
                case 2: return FragmentSong.TITLE;
                case 3: return FragmentMyLists.TITLE;
            }
            return super.getPageTitle(position);
        }
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() { }

        public static Fragment newInstance(int sectionNumber) {
            switch (sectionNumber){
                case 1: return new FragmentSettings();
                case 2: return new FragmentList();
                case 3: return new FragmentSong();
                case 4: return new FragmentMyLists();
            }
            return null;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

}
