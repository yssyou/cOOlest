package com.example.rayku.coolest;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.media.session.MediaControllerCompat;

public class SeekBarTask extends AsyncTask<MediaControllerCompat.TransportControls, Void, Void>{

    MediaControllerCompat.TransportControls tpControls;

    @Override
    protected Void doInBackground(MediaControllerCompat.TransportControls... o) {
        tpControls = o[0];
        while (!isCancelled()) {
            publishProgress();
            SystemClock.sleep(400);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        tpControls.sendCustomAction("refreshSeekBarPlz", null);
    }
}
