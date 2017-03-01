package com.owlcreativestudio.unify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.owlcreativestudio.unify.Helpers.DownloadImageTask;
import com.owlcreativestudio.unify.Services.CameraService;
import com.owlcreativestudio.unify.Services.LocationService;
import com.owlcreativestudio.unify.Services.PositionService;

public class FullscreenActivity extends AppCompatActivity {
    FrameLayout contentLayout;
    private boolean isVisible;

    private final Handler mHideHandler = new Handler();
    private FrameLayout cameraLayout;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String TAG = "FullscreenActivity";
    private View masterLayout;
    private View controlsLayout;

    private CameraService cameraService;
    private LocationService locationService;
    private PositionService positionService;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            masterLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            controlsLayout.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        //layouts
        masterLayout = findViewById(R.id.master_layout);
        cameraLayout = (FrameLayout) findViewById(R.id.camera_layout);
        contentLayout = (FrameLayout) findViewById(R.id.content_layout);
        controlsLayout = findViewById(R.id.controls_layout);

        isVisible = true;

        masterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        findViewById(R.id.settings_button).setOnTouchListener(mDelayHideTouchListener);


        cameraService = new CameraService();
        locationService = new LocationService();
        positionService = new PositionService(this);

        //test section
        setARElements();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraService.releaseCamera();
        positionService.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            cameraService.initializeCamera(this, cameraLayout);
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        try {
            locationService.startGPSTracking(this);
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        positionService.startPositionSensors();
    }

    public void settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void toggle() {
        if (isVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        controlsLayout.setVisibility(View.GONE);
        isVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        masterLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        isVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void setARElements() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(400, 400);
        layoutParams.setMargins(400, 400, 0, 0);

        ImageButton iButton = new ImageButton(this);
        iButton.setLayoutParams(layoutParams);
        contentLayout.addView(iButton);

        new DownloadImageTask(iButton).execute("https://avatars3.githubusercontent.com/u/13658952?v=3&s=460");
    }
}
