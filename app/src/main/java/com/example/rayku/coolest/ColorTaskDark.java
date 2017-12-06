package com.example.rayku.coolest;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

class ColorTaskDark {

    private Random random;
    private int color;
    private ShiftColorTask task0;
    private ColorTask task1;

    ColorTaskDark(ThreadPoolExecutor mThreadPoolExecutor, int delay, int colorDelay,
                  View bg1, View bg2, View bg3, View bg4){

        random = new Random();

        color = random.nextInt(6);
        task0 = new ShiftColorTask();
        task1 = new ColorTask();

        task0.executeOnExecutor(mThreadPoolExecutor, colorDelay);
        task1.executeOnExecutor(mThreadPoolExecutor, new ObjectToColorTask(bg1, bg2, bg3, bg4, delay));

    }

    void killColorTaskDark(){
        task0.cancel(true);
        task1.cancel(true);
    }

    private class ColorTask extends AsyncTask<ObjectToColorTask, Void, Void> {
        View bg1, bg2, bg3, bg4;
        int delay;
        @Override
        protected Void doInBackground(ObjectToColorTask... params) {
            bg1 = params[0].getView1();
            bg2 = params[0].getView2();
            bg3 = params[0].getView3();
            bg4 = params[0].getView4();
            delay = params[0].getDelay();
            while (!isCancelled()) {
                publishProgress();
                SystemClock.sleep(delay);
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
        if(color == 0) view.setBackgroundColor(Color.rgb(random.nextInt(50)+10, 0, 0));
        if(color == 1) view.setBackgroundColor(Color.rgb(random.nextInt(50)+10, random.nextInt(50)+10, 0));
        if(color == 2) view.setBackgroundColor(Color.rgb(0, random.nextInt(50)+10, 0));
        if(color == 3) view.setBackgroundColor(Color.rgb(0, random.nextInt(50)+10, random.nextInt(50)+10));
        if(color == 4) view.setBackgroundColor(Color.rgb(0, 0, random.nextInt(50)+10));
        if(color == 5) view.setBackgroundColor(Color.rgb(random.nextInt(50)+10, 0, random.nextInt(50)+10));
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
