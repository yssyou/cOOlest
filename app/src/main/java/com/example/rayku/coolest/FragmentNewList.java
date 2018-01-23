package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class FragmentNewList extends Fragment {

    private OnFragmentInteractionListener mListener;

    ArrayList<Song> songsList;
    ArrayList<Long> theIDs;
    SongsListAdapter adapter2;
    ListView listView;
    EditText editText;
    TextView textView;
    Typeface typeface;
    int spTheme;
    Button createBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        typeface = mListener.getTypeface();
        spTheme = mListener.getSpTheme();

        listView = getView().findViewById(R.id.listView);
        editText = getView().findViewById(R.id.editText);
        textView = getView().findViewById(R.id.textView);
        createBtn = getView().findViewById(R.id.createBtn);

        editText.setTypeface(typeface);
        textView.setTypeface(typeface);
        createBtn.setTypeface(typeface);


        songsList = mListener.getSongList();
        theIDs = mListener.getTheIDs();

        adapter2 = new SongsListAdapter(getContext(), songsList, typeface, spTheme);

        listView.setAdapter(adapter2);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Song s = (Song) adapterView.getItemAtPosition(i);

                if (view.getBackground() == null) {
                    theIDs.add(s.getId());
                    adapter2.select(i, true);
                } else {
                    theIDs.remove(s.getId());
                    adapter2.select(i, false);
                }
            }
        });

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

    interface OnFragmentInteractionListener {
        int getSpTheme();
        Typeface getTypeface();
        ArrayList<Song> getSongList();
        ArrayList<Long> getTheIDs();
    }
}
