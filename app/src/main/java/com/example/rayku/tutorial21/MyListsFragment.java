package com.example.rayku.tutorial21;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class MyListsFragment extends Fragment {

    public static final String TITLE = "MIS LISTAS";

    private OnFragmentInteractionListener mListener;

    ArrayList<Song> arrayList;

    ArrayList<String> listsNames;
    ArrayAdapter<String> adapter;

    GridView listsGridView;

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

        arrayList = mListener.getSongList();
        listsNames = new ArrayList<>();
        listsNames.add("+");

        listsNames.add("kpop");
        listsNames.add("reggaeton");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listsGridView = getView().findViewById(R.id.listsGridView);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listsNames);

        listsGridView.setAdapter(adapter);
    }




    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnFragmentInteractionListener {
        ArrayList<Song> getSongList();
    }
}
