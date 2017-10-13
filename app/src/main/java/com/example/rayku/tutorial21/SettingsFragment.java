package com.example.rayku.tutorial21;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsFragment extends Fragment implements View.OnClickListener{

    public static final String TITLE = "OPCIONES";

    private OnFragmentInteractionListener mListener;

    View rootView;
    TextView theme0, theme1;

    Typeface typeFace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "Amatic-Bold.ttf");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        return rootView;
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
        void switchTheme(int i);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        theme0 = rootView.findViewById(R.id.theme0);
        theme1 = rootView.findViewById(R.id.theme1);
        theme0.setOnClickListener(this);
        theme1.setOnClickListener(this);
        theme0.setTypeface(typeFace);
        theme1.setTypeface(typeFace);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.theme0:
                mListener.switchTheme(0);
                break;
            case R.id.theme1:
                mListener.switchTheme(1);
                break;
        }
    }
}
