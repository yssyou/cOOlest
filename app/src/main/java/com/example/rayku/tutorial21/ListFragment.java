package com.example.rayku.tutorial21;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ListFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1, mParam2;

    private OnFragmentInteractionListener mListener;

    View rootView;

    View listPlayBtn;
    TextView playingSongTitle, playingSongArtist;

    ArrayList<Song> arrayList;

    Activity mainActivity;

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    public ListFragment() { }

    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mainActivity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpLayout();
        getSongList();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        arrayList = ((MainActivity) mainActivity).getSongList();
        ListView listView = rootView.findViewById(R.id.listView);
        SongAdapter adapter = new SongAdapter(getContext(), arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((MainActivity) mainActivity).playSong(i);
                updateInterface(i);
            }
        });
    }

    public void updateInterface(int i){
        Song currentSong = arrayList.get(i);
        playingSongTitle.setText(currentSong.getTitle());
        playingSongArtist.setText(currentSong.getArtist());
        listPlayBtn.setBackgroundResource(R.drawable.pause);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.listPlayBtn:
                ((MainActivity)mainActivity).playPause(null);
                if( ((MainActivity)mainActivity).getCurrentState() == STATE_PLAYING ) {
                    listPlayBtn.setBackgroundResource(R.drawable.pause);
                } else {
                    listPlayBtn.setBackgroundResource(R.drawable.play);
                }
                break;
        }
    }






    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
