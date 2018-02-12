package com.example.rayku.coolest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.schedulers.Schedulers;

public class BackgroundAudioService extends MediaBrowserServiceCompat
        implements MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSessionCompat;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }
    };

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            if (!successfullyRetrievedAudioFocus()) {
                return;
            }
            mediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

            try {
                showPlayingNotification();
            } catch (Exception startingProgramException){
                startingProgramException.printStackTrace();
            }
            mediaPlayer.start();
        }

        @Override
        public void onPause() {
            super.onPause();
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                showPausedNotification();
            }
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);

            try {
                try {
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                } catch(IllegalStateException e){
                    mediaPlayer.release();
                    initMediaPlayer();
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                }
                initMediaSessionMetadata(extras.getString("title"), extras.getString("artist"));
            } catch (IOException e){
                return;
            }

            try {
                mediaPlayer.prepare();
            } catch(IOException e) { e.printStackTrace(); }

        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mediaPlayer.seekTo((int)pos);
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            mediaSessionCompat.sendSessionEvent("playPrevSong", null);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            mediaSessionCompat.sendSessionEvent("playNextSong", null);
        }

    };

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();

        Bundle extras = new Bundle();
        io.reactivex.Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aVoid -> {
                    extras.putInt("duration", mediaPlayer.getDuration());
                    extras.putInt("position", mediaPlayer.getCurrentPosition());
                    mediaSessionCompat.sendSessionEvent("refreshSeekBar", extras);
                });
    }

    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.setOnCompletionListener(this);
    }

    private void initMediaSession(){
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mediaSessionCompat.getSessionToken());
    }

    private void initNoisyReceiver(){
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(broadcastReceiver, filter);
    }

    private boolean successfullyRetrievedAudioFocus(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        assert audioManager != null;
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ){

            case AudioManager.AUDIOFOCUS_LOSS: {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaSessionCompat.sendSessionEvent("lostAudioFocus", null);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                showPausedNotification();
                break;
            }

            // LOSS_TRANSIENT_CAN_DUCK had some issues so it got removed.

        }
    }

    private void setMediaPlaybackState(int state){
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();

        if(state == PlaybackStateCompat.STATE_PLAYING){
            builder.setActions(
                    PlaybackStateCompat.ACTION_PAUSE |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        }else{
            builder.setActions(
                    PlaybackStateCompat.ACTION_PLAY |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        }

        builder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSessionCompat.setPlaybackState(builder.build());
    }

    private void showPlayingNotification(){
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSessionCompat);

        builder.addAction(new android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()));

        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(BackgroundAudioService.this).notify(1, builder.build());
    }

    private void showPausedNotification(){
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSessionCompat);

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(this).notify(1, builder.build());

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaSessionCompat.sendSessionEvent("songFinished", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        assert audioManager != null;
        audioManager.abandonAudioFocus(this);

        unregisterReceiver(broadcastReceiver);
        mediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(1);
    }

    private void initMediaSessionMetadata(String title, String artist) {

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.coolest_icon));
        //builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);

        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, artist);
        builder.putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, artist);

        builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title);
        builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist);

        mediaSessionCompat.setMetadata(builder.build());
    }

}
