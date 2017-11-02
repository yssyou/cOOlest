package com.example.rayku.tutorial21;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MyListsFragment extends Fragment {

    public static final String TITLE = "MIS LISTAS";

    private OnFragmentInteractionListener mListener;

    ArrayList<Song> arrayList;

    HashMap<String, ArrayList<Song>> lists;
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

        arrayList = mListener.getSongList();
        lists = mListener.getLists();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listsGridView = getView().findViewById(R.id.listsGridView);

        ArrayList<String> listTitles = new ArrayList<>();
        listTitles.addAll(lists.keySet());
        adapter = new ListsGridAdapter(getContext(), listTitles);
        listsGridView.setAdapter(adapter);

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnFragmentInteractionListener {
        ArrayList<Song> getSongList();
        HashMap<String, ArrayList<Song>> getLists();
        void createNewList(String name, ArrayList<Song> list);
    }
}
