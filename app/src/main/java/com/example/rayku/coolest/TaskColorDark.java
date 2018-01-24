package com.example.rayku.coolest;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

class TaskColorDark {

    private Random random;
    private int color;
    private ShiftColorTask task0;
    private ColorTask task1;

    TaskColorDark(ThreadPoolExecutor mThreadPoolExecutor, View bg1, View bg2, View bg3, View bg4){

        random = new Random();

        color = random.nextInt(6);
        task0 = new ShiftColorTask();
        task1 = new ColorTask();

        task0.executeOnExecutor(mThreadPoolExecutor, 2200);
        task1.executeOnExecutor(mThreadPoolExecutor, new ObjectToTaskColor(bg1, bg2, bg3, bg4, 2200));

    }

    void killColorTaskDark(){
        task0.cancel(true);
        task1.cancel(true);
    }

    private class ColorTask extends AsyncTask<ObjectToTaskColor, Void, Void> {
        View bg1, bg2, bg3, bg4;
        int delay;
        @Override
        protected Void doInBackground(ObjectToTaskColor... params) {
            bg1 = params[0].getView1();
            bg2 = params[0].getView2();
            bg3 = params[0].getView3();
            bg4 = params[0].getView4();
            delay = params[0].getDelay();
            while (!isCancelled()) {
                publishProgress();
                SystemClock.sleep(delay - random.nextInt(2000));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            chooseColor(bg1);
            chooseColor(bg2);
            chooseColor(bg3);
            chooseColor(bg4);
        }
    }

    private void chooseColor(View view){

        int intensity = 40;
        int lowIntensity = 15;

        if(color == 0) view.setBackgroundColor(Color.rgb(intensity, 0, 0));
        if(color == 1) view.setBackgroundColor(Color.rgb(intensity, intensity/2, 0));
        if(color == 2) view.setBackgroundColor(Color.rgb(0, intensity/2, 0));
        if(color == 3) view.setBackgroundColor(Color.rgb(0, intensity/2, intensity));
        if(color == 4) view.setBackgroundColor(Color.rgb(0, 0, intensity));
        if(color == 5) view.setBackgroundColor(Color.rgb(intensity, 0, intensity));


    }

    private class ShiftColorTask extends AsyncTask<Integer, Void, Void>{
        protected Void doInBackground(Integer... params) {
            while (!isCancelled()) {
                publishProgress();
                SystemClock.sleep(params[0]);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(color>4){
                color = 0;
            }else{
                color++;
            }
        }
    }

}
