package com.example.rayku.tutorial21;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.SeekBar;

class SeekBarTask extends AsyncTask<SeekBar, Void, Void> {

        SeekBar seekBar;

        @Override
        protected Void doInBackground(SeekBar... params) {
            seekBar = params[0];

            while(!isCancelled()) {
                publishProgress();
                SystemClock.sleep(400); // COULD CHANGE THAT VALUE
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            seekBar.setProgress( seekBar.getProgress()+400 );
        }

}