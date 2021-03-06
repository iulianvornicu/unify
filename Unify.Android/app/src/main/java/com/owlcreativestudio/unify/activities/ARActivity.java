package com.owlcreativestudio.unify.activities;

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
import android.widget.LinearLayout;

import com.owlcreativestudio.unify.R;
import com.owlcreativestudio.unify.models.AdjacentPerson;
import com.owlcreativestudio.unify.models.FacebookProfile;
import com.owlcreativestudio.unify.models.UnifyLocation;
import com.owlcreativestudio.unify.services.ARService;
import com.owlcreativestudio.unify.services.CameraService;
import com.owlcreativestudio.unify.services.LocationService;
import com.owlcreativestudio.unify.services.PositionService;
import com.owlcreativestudio.unify.services.ProfileDetailsService;

import java.util.ArrayList;
import java.util.List;

public class ARActivity extends AppCompatActivity {
    private boolean isVisible;

    private final Handler mHideHandler = new Handler();
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String TAG = "ARActivity";
    private View masterLayout;
    private View controlsLayout;

    private ARService arService;
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

        setContentView(R.layout.activity_ar);
        hideActionBar();

        //set layouts
        masterLayout = findViewById(R.id.master_layout);
        controlsLayout = findViewById(R.id.controls_layout);
        FrameLayout cameraLayout = (FrameLayout) findViewById(R.id.camera_layout);
        FrameLayout arLayout = (FrameLayout) findViewById(R.id.ar_layout);
        LinearLayout appProfileLayout = (LinearLayout) findViewById(R.id.app_profile_layout);
        LinearLayout facebookProfileLayout = (LinearLayout) findViewById(R.id.facebook_profile_layout);
        LinearLayout linkedInProfileLayout = (LinearLayout) findViewById(R.id.linkedin_profile_layout);
        LinearLayout twitterProfileLayout = (LinearLayout) findViewById(R.id.twitter_profile_layout);
        LinearLayout navigationProfilesLayout = (LinearLayout) findViewById(R.id.navigation_profiles_layout);

        isVisible = true;

        //set services
        ProfileDetailsService profileDetailsService = new ProfileDetailsService(this, arLayout, appProfileLayout, facebookProfileLayout, linkedInProfileLayout, twitterProfileLayout, navigationProfilesLayout);
        arService = new ARService(this, 10, 50, arLayout, profileDetailsService);
        cameraService = new CameraService(this, cameraLayout, arService);
        locationService = new LocationService(this, arService);
        positionService = new PositionService(this, arService);


        //set behaviour
        masterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        findViewById(R.id.settings_button).setOnTouchListener(mDelayHideTouchListener);

        //set fake data
        setFakeData();
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
            cameraService.initializeCamera();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        try {
            locationService.startGPSTracking();
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
        controlsLayout.setVisibility(View.GONE);
        isVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        masterLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        isVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void setFakeData() {


        List<AdjacentPerson> adjacentPeople = new ArrayList<>();
        adjacentPeople.add(getPerson1());
        adjacentPeople.add(getPerson2());
        adjacentPeople.add(getPerson3());

        arService.setAdjacentPeople(adjacentPeople);
    }

    private AdjacentPerson getPerson1() {
        UnifyLocation location = new UnifyLocation();
        location.setElevation(0);
        location.setLatitude(44.4317468);
        location.setLongitude(26.0188011);
//        location.setLatitude(45);
//        location.setLongitude(45);

        FacebookProfile fbp = new FacebookProfile();
        fbp.setName("Qutory");
        fbp.setPictureLink("https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/1544311_831950490182533_4983436575430649866_n.jpg?oh=1a85597d84b8b2534be700249d10836b&oe=59636704");

        AdjacentPerson adjacentPerson = new AdjacentPerson();
        adjacentPerson.setDisplayName("Qutory");
        adjacentPerson.setImageUrl("https://avatars3.githubusercontent.com/u/13658952?v=3&s=460");
        adjacentPerson.setId("" + Math.random());
        adjacentPerson.setLocation(location);
        adjacentPerson.setFacebookProfile(fbp);


        return adjacentPerson;
    }

    private AdjacentPerson getPerson2() {
        UnifyLocation location = new UnifyLocation();
        location.setElevation(0);
        location.setLatitude(44.431785);
        location.setLongitude(26.01896);
//        location.setLatitude(-45);
//        location.setLongitude(-45);

        AdjacentPerson adjacentPerson = new AdjacentPerson();
        adjacentPerson.setDisplayName("ADL");
        adjacentPerson.setImageUrl("https://scontent.fotp3-2.fna.fbcdn.net/v/t1.0-1/p160x160/14463131_1174090729337640_5968235094959303848_n.jpg?oh=6d524d8f0a743f059a46d4b0b59766a5&oe=5966C6A6");
        adjacentPerson.setId("" + Math.random());
        adjacentPerson.setLocation(location);

        return adjacentPerson;
    }

    private AdjacentPerson getPerson3() {
        UnifyLocation location = new UnifyLocation();
        location.setElevation(0);
        location.setLatitude(45);
        location.setLongitude(45);

        AdjacentPerson adjacentPerson = new AdjacentPerson();
        adjacentPerson.setDisplayName("Qutory");
        adjacentPerson.setImageUrl("https://avatars3.githubusercontent.com/u/13658952?v=3&s=460");
        adjacentPerson.setId("" + Math.random());
        adjacentPerson.setLocation(location);

        return adjacentPerson;
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

}
