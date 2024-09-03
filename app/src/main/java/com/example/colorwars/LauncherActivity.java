package com.example.colorwars;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.colorwars.classes.MediaPlayerSingleton;

public class LauncherActivity extends AppCompatActivity {
    private ImageView logo;
    private TextView t;
    private Button easy, medium, hard;
    Animation topAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.main));
        setContentView(R.layout.activity_launcher);
        logo = findViewById(R.id.imageView);
        t = findViewById(R.id.textView);
        easy = findViewById(R.id.Easy);
        medium = findViewById(R.id.Medium);
        hard = findViewById(R.id.Hard);
        final MediaPlayer mediaPlayer=MediaPlayer.create(this,R.raw.modesound);
        MediaPlayerSingleton back = MediaPlayerSingleton.getInstance(this);
        back.setVolume(0.5f);
        back.start();
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);

        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        logo.setAnimation(topAnim);
        t.setAnimation(topAnim);
        easy.setAnimation(bottomAnim);
        medium.setAnimation(bottomAnim);
        hard.setAnimation(bottomAnim);
        Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
        easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                intent.putExtra("key", "easy");
                startActivity(intent);
            }
        });
        medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                intent.putExtra("key", "medium");
                startActivity(intent);
            }
        });
        hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                intent.putExtra("key", "hard");
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Pause the music if desired
        MediaPlayerSingleton.getInstance(this).pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the music
        MediaPlayerSingleton.getInstance(this).start();
    }
    @Override
    public void onBackPressed() {
        // Create an alert dialog
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit the activity
                        LauncherActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}