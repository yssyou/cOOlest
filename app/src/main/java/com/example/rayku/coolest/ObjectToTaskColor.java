package com.example.rayku.coolest;

import android.view.View;

class ObjectToTaskColor {
    private View view1, view2, view3, view4;
    private int delay;

    ObjectToTaskColor(View view1, View view2, View view3, View view4, int delay){
        this.view1 = view1;
        this.view2 = view2;
        this.view3 = view3;
        this.view4 = view4;
        this.delay = delay;
    }

    View getView1(){ return view1; }
    View getView2(){ return view2; }
    View getView3(){ return view3; }
    View getView4(){ return view4; }
    int getDelay(){ return delay; }
}
