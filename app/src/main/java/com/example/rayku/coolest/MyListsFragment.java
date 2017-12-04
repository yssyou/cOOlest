package com.example.rayku.coolest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;

public class MyListsFragment extends Fragment{

    public static final String TITLE = "MY LISTS";

    private OnFragmentInteractionListener mListener;

    ArrayList<String> listsTitles;
    ListsGridAdapter adapter;

    GridView listsGridView;
    Typeface typeFace;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mylists, container, false);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "Amatic-Bold.ttf");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateInterface();
    }

    public void updateInterface(){
        listsGridView = getView().findViewById(R.id.listsGridView);

        listsTitles = new ArrayList<>();
        listsTitles.addAll(mListener.getLists().keySet());

        adapter = new ListsGridAdapter(getContext(), listsTitles, mListener.getTypeface());
        listsGridView.setAdapter(adapter);

        listsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(view.getBackground()==null) {
                    view.setBackgroundColor(Color.argb(60, 0, 0, 0));
                } else view.setBackground(null);
                mListener.setList(listsGridView.getItemAtPosition(i).toString());
            }
        });

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnFragmentInteractionListener {
        HashMap<String, ArrayList<Long>> getLists();
        void setList(String listName);
        Typeface getTypeface();
    }
}
