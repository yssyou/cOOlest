package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentNewList extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    ArrayList<Song> songsList;
    ArrayList<Long> theIDs;
    AdapterSongsList adapter;
    ListView listView;
    EditText editText;
    TextView textView;
    Typeface typeface;
    int spTheme;
    Button createBtn;
    SearchView searchView;
    ImageView searchIcon;
    ImageView searchCloseIcon;


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
        searchView = getView().findViewById(R.id.searchView);
        searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchCloseIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        createBtn.setOnClickListener(this);

        editText.setTypeface(typeface);
        textView.setTypeface(typeface);
        createBtn.setTypeface(typeface);

        updateTheme();

        songsList = mListener.getSongList();
        theIDs = new ArrayList<>();

        adapter = new AdapterSongsList(getContext(), songsList, typeface, spTheme);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Song s = (Song) adapterView.getItemAtPosition(i);

                if (view.getBackground() == null) {
                    if(!theIDs.contains(s.getId())) {
                        theIDs.add(s.getId());
                        adapter.select(s, true);
                    }
                } else {
                    theIDs.remove(s.getId());
                    adapter.select(s, false);
                }
            }
        });

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

    private void updateTheme() {
        int color;
        if(spTheme==0||spTheme==1){
            color = getResources().getColor(android.R.color.black);
        }else{
            color = getResources().getColor(android.R.color.white);
        }

        editText.setTextColor(color);
        editText.setHintTextColor(color);
        //editText.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        editText.setHighlightColor(color);

        textView.setTextColor(color);
        createBtn.setTextColor(color);

        customizeSearchView(color);
    }

    private void customizeSearchView(int textColor){
        ((TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(textColor);
        searchIcon.setColorFilter(textColor);
        searchCloseIcon.setColorFilter(textColor);
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
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.createBtn:

                String name = editText.getText().toString();
                if(name.length()==0){
                    editText.setError("Name must not be empty");
                    editText.requestFocus();
                    return;
                }
                if(theIDs.size()==0){
                    Toast.makeText(getContext(), "The playlist must not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                mListener.createNewList(theIDs, name);
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                break;
        }
    }

    interface OnFragmentInteractionListener {
        int getSpTheme();
        Typeface getTypeface();
        ArrayList<Song> getSongList();
        void createNewList(ArrayList<Long> theIDs, String listTitle);
    }
}
