package com.example.rayku.tutorial21;

import android.view.View;

class ObjectToColorTask {
    private View view;
    private int delay;

    ObjectToColorTask(View view, int delay){
        this.view = view;
        this.delay = delay;
    }

    View getView(){ return view; }
    int getDelay(){ return delay; }
}
