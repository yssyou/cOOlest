package com.example.rayku.coolest;

import android.os.AsyncTask;
import android.os.SystemClock;

class PermissionsTask extends AsyncTask<Boolean, Void, Void> {

    boolean accepted;

    @Override
    protected Void doInBackground(Boolean... params) {

        accepted = params[0];

        while (!accepted) {
            publishProgress();
            SystemClock.sleep(5000);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {

    }
}


