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
import android.widget.TextView;

public class FragmentSettings extends Fragment implements View.OnClickListener{

    public static final String TITLE = "SETTINGS";

    private OnFragmentInteractionListener mListener;

    View rootView;
    TextView theme00, theme01, theme10, theme11;

    Typeface typeFace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "Ubuntu-C.ttf");

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
        int getSpTheme();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupLayout();
        updateTheme();
    }

    public void setupLayout(){
        theme00 = rootView.findViewById(R.id.theme00); theme01 = rootView.findViewById(R.id.theme01);
        theme10 = rootView.findViewById(R.id.theme10); theme11 = rootView.findViewById(R.id.theme11);

        theme00.setOnClickListener(this); theme01.setOnClickListener(this);
        theme10.setOnClickListener(this); theme11.setOnClickListener(this);
        theme00.setTypeface(typeFace); theme01.setTypeface(typeFace);
        theme10.setTypeface(typeFace); theme11.setTypeface(typeFace);
    }

    public void updateTheme(){

        int theme = mListener.getSpTheme();
        int textColor, bgColorLight, bgColorDark;

        if(theme==0 || theme==1){
            textColor = Color.BLACK;
            bgColorLight = Color.argb(20, 0, 0, 0);
            bgColorDark = Color.argb(60, 0, 0, 0);
        } else{
            textColor = Color.WHITE;
            bgColorLight = Color.argb(20, 255, 255, 255);
            bgColorDark = Color.argb(60, 255, 255, 255);
        }

        theme00.setTextColor(textColor); theme01.setTextColor(textColor);
        theme10.setTextColor(textColor); theme11.setTextColor(textColor);

        switch(theme){
            case 0:
                theme00.setBackgroundColor(bgColorDark);
                theme01.setBackgroundColor(bgColorLight);
                theme10.setBackgroundColor(bgColorLight);
                theme11.setBackgroundColor(bgColorLight);
                break;
            case 1:
                theme01.setBackgroundColor(bgColorDark);
                theme00.setBackgroundColor(bgColorLight);
                theme10.setBackgroundColor(bgColorLight);
                theme11.setBackgroundColor(bgColorLight);
                break;
            case 2:
                theme10.setBackgroundColor(bgColorDark);
                theme00.setBackgroundColor(bgColorLight);
                theme01.setBackgroundColor(bgColorLight);
                theme11.setBackgroundColor(bgColorLight);
                break;
            case 3:
                theme11.setBackgroundColor(bgColorDark);
                theme00.setBackgroundColor(bgColorLight);
                theme01.setBackgroundColor(bgColorLight);
                theme10.setBackgroundColor(bgColorLight);
                break;
        }

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.theme00:
                mListener.switchTheme(0);
                break;
            case R.id.theme01:
                mListener.switchTheme(1);
                break;
            case R.id.theme10:
                mListener.switchTheme(2);
                break;
            case R.id.theme11:
                mListener.switchTheme(3);
                break;
        }
    }

}
