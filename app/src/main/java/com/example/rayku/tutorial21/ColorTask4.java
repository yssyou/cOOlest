package com.example.rayku.tutorial21;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

class ColorTask4 {

    private Random random;
    private int color;
    private ShiftColorTask task0;
    private ColorTask task1, task2, task3, task4;

    ColorTask4(ThreadPoolExecutor mThreadPoolExecutor, int delay, int colorDelay,
               View bg1, View bg2, View bg3, View bg4){

        random = new Random();

        color = random.nextInt(6);
        task0 = new ShiftColorTask();
        task1 = new ColorTask();
        task2 = new ColorTask();
        task3 = new ColorTask();
        task4 = new ColorTask();

        task0.executeOnExecutor(mThreadPoolExecutor, colorDelay);
        task1.executeOnExecutor(mThreadPoolExecutor, new ObjectToColorTask(bg1, delay));
        task2.executeOnExecutor(mThreadPoolExecutor, new ObjectToColorTask(bg2, delay));
        task3.executeOnExecutor(mThreadPoolExecutor, new ObjectToColorTask(bg3, delay));
        task4.executeOnExecutor(mThreadPoolExecutor, new ObjectToColorTask(bg4, delay));

    }

    void killLightColorTask(){
        task0.cancel(true);
        task1.cancel(true);
        task2.cancel(true);
        task3.cancel(true);
        task4.cancel(true);
    }

    private class ColorTask extends AsyncTask<ObjectToColorTask, Void, Void> {
        View bg;
        int delay;
        @Override
        protected Void doInBackground(ObjectToColorTask... params) {
            bg = params[0].getView();
            delay = params[0].getDelay();
            while (!isCancelled()) {
                publishProgress();
                SystemClock.sleep(delay);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            chooseColor(bg);
        }
    }

    private void chooseColor(View view){
        if(color == 0) view.setBackgroundColor(Color.rgb(random.nextInt(50)+200, 0, 0));
        if(color == 1) view.setBackgroundColor(Color.rgb(random.nextInt(50)+200, random.nextInt(50)+200, 0));
        if(color == 2) view.setBackgroundColor(Color.rgb(0, random.nextInt(50)+200, 0));
        if(color == 3) view.setBackgroundColor(Color.rgb(0, random.nextInt(50)+200, random.nextInt(50)+200));
        if(color == 4) view.setBackgroundColor(Color.rgb(0, 0, random.nextInt(50)+200));
        if(color == 5) view.setBackgroundColor(Color.rgb(random.nextInt(50)+200, 0, random.nextInt(50)+200));
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
