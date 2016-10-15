package com.secuso.torchlight2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    private boolean flashState = false;
    private ImageButton btnSwitch;
    private boolean endWhenPaused;
    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;

    private Parameters p;

    private Camera camera;

    private boolean isConnected;

    private Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("firstShow", "").commit();
        SharedPreferences settings = getSharedPreferences("firstShow", getBaseContext().MODE_PRIVATE);
        if (settings.getBoolean("isFirstRun", true)) {

            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(getFragmentManager(), "WelcomeDialog");

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }

        if(!isConnected) init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void init() {
        Context context = this;
        PackageManager pm = context.getPackageManager();

        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) | !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e("err", "Device has no camera!");
            Toast.makeText(this, R.string.no_flash,Toast.LENGTH_LONG);
            btnSwitch.setEnabled(false);
            return;
        }

        if(ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                if(camera == null || !isConnected || p == null) {
                    camera = Camera.open();
                    isConnected = true;
                    p = camera.getParameters();
                }
            } catch(RuntimeException e) {
                Toast.makeText(this, "Can not get the camera" ,Toast.LENGTH_LONG);
            }
        }

        btnSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (ContextCompat.checkSelfPermission(thisActivity,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.CAMERA},0) ;
                }

                if(camera != null) {
                    toggleCamera();
                }

            }
        });
    }

    private void toggleCamera() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        camera = Camera.open();
                        isConnected = true;
                        p = camera.getParameters();
                        toggleCamera();
                    } catch(RuntimeException e) {
                        Toast.makeText(this, "An unknown error occured." ,Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Can not use flashlight without access to the camera." ,Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!flashState)
            close();
        else if(endWhenPaused) {
            stop();
        }
    }

    private void stop(){
        p.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        camera.stopPreview();
        flashState = false;
        isConnected = false;
        camera.release();
        camera = null;
    }

    private void close(){
        flashState = false;
        if(isConnected)
            camera.release();
        isConnected = false;
        camera = null;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isConnected) init();
        if(flashState)
            btnSwitch.setImageDrawable(getResources().getDrawable(R.drawable.on));
        else
            btnSwitch.setImageDrawable(getResources().getDrawable(R.drawable.off));
    }

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }


    public static class WelcomeDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(i.inflate(R.layout.welcome_dialog, null));
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle(getActivity().getString(R.string.welcome));
            builder.setPositiveButton(getActivity().getString(R.string.okay), null);
            builder.setNegativeButton(getActivity().getString(R.string.viewhelp), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((MainActivity)getActivity()).goToNavigationItem(R.id.nav_help);
                }
            });

            return builder.create();
        }
    }
}
