package com.secuso.torchlight;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;

public class MainActivity extends ActionBarActivity {

    private boolean flashState;
    private ImageButton btnSwitch;
    private boolean endWhenPaused;
    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;

    private Parameters p;

    private Camera camera;

    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.mipmap.ic_launcher);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#024265")));

        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
        preferences = this.getPreferences(Context.MODE_PRIVATE);
        prefEditor = preferences.edit();
        flashState = false;
        endWhenPaused = preferences.getBoolean("closeOnPause", false);

        CheckBox pauseState = (CheckBox) findViewById(R.id.cbPause);

        pauseState.setChecked(endWhenPaused);

        pauseState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endWhenPaused = !endWhenPaused;
                prefEditor.putBoolean("closeOnPause", endWhenPaused);
                prefEditor.commit();
            }
        });

        if(!isConnected) init();
    }

    @Override
    protected void onPause() {
        if(flashState && endWhenPaused){
            close();
            btnSwitch.setImageDrawable(getResources().getDrawable(R.drawable.off));
        }
        else if(!flashState)
            camera.release();
        super.onPause();
    }


    private void init() {
        Context context = this;
        PackageManager pm = context.getPackageManager();

        flashState = false;

        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            return;
        }

        camera = Camera.open();
        isConnected = true;
        p = camera.getParameters();

        btnSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (flashState) {
                    //"info", "torch is turn off!"
                    p.setFlashMode(Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    camera.stopPreview();
                    flashState = false;
                    btnSwitch.setImageDrawable(getResources().getDrawable(R.drawable.off));

                } else {
                    //"info", "torch is turn on!"
                    p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);
                    camera.startPreview();
                    flashState = true;
                    btnSwitch.setImageDrawable(getResources().getDrawable(R.drawable.on));
                }

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(endWhenPaused) {
            camera.release();
            isConnected = false;
        }
    }

    private void close(){
        p.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        camera.stopPreview();
        flashState = false;
        isConnected = false;
        camera.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isConnected) init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intent = new Intent();
                intent.setClass(this, AboutActivity.class);
                close();
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
