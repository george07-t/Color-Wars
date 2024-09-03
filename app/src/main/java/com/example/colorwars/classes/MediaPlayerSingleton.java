package com.example.colorwars.classes;


import android.content.Context;
import android.media.MediaPlayer;

import com.example.colorwars.R;

public class MediaPlayerSingleton {
    private static MediaPlayerSingleton instance;
    private MediaPlayer mediaPlayer;

    private MediaPlayerSingleton(Context context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.back);
        mediaPlayer.setLooping(true); // Optional: Loop the music
    }

    public static synchronized MediaPlayerSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new MediaPlayerSingleton(context.getApplicationContext());
        }
        return instance;
    }

    public void start() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            instance = null;
        }
    }
}

