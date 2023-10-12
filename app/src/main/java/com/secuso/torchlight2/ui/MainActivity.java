/**
 * This file is part of Privacy Friendly Torchlight.
 * Privacy Friendly Torchlight is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Torchlight is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Torchlight. If not, see <http://www.gnu.org/licenses/>.
 */

package com.secuso.torchlight2.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.secuso.torchlight2.R;
import com.secuso.torchlight2.camera.CameraMarshmallow;
import com.secuso.torchlight2.camera.CameraNormal;
import com.secuso.torchlight2.camera.ICamera;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends BaseActivity {

    private boolean flashState = false;
    private ImageButton btnSwitch;
    private boolean endWhenPaused;
    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;
    private ICamera mCamera;
    private boolean isConnected;
    private Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
        preferences = this.getPreferences(MODE_PRIVATE);
        prefEditor = preferences.edit();
        flashState = false;
        endWhenPaused = preferences.getBoolean("closeOnPause", false);

        CheckBox pauseState = (CheckBox) findViewById(R.id.cbPause);
        if (pauseState != null) {
            pauseState.setChecked(endWhenPaused);
            pauseState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endWhenPaused = !endWhenPaused;
                    prefEditor.putBoolean("closeOnPause", endWhenPaused);
                    prefEditor.commit();
                }
            });
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("firstShow", "").apply();
        SharedPreferences settings = getSharedPreferences("firstShow", getBaseContext().MODE_PRIVATE);

        if (settings.getBoolean("isFirstRun", true)) {
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(getFragmentManager(), "WelcomeDialog");

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }

        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void init() {
        PackageManager pm = getPackageManager();

        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) | !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e("err", "Device has no camera!");
            Toast.makeText(this, R.string.no_flash, Toast.LENGTH_LONG).show();
            btnSwitch.setEnabled(false);
            return;
        }

        // Check Android Version
        if (SDK_INT >= Build.VERSION_CODES.M) {
            mCamera = new CameraMarshmallow();
        } else {
            mCamera = new CameraNormal();
        }

        // set up camera
        setUpCamera();

        btnSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // can we have permissions that are revoked?
                if (SDK_INT >= Build.VERSION_CODES.M) {
                    // check if we have the permission we need -> if not request it and turn on the light afterwards
                    if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.CAMERA}, 0);
                        return;
                    }
                }

                toggleCamera(!flashState);
            }
        });
    }

    private void setUpCamera() {
        mCamera.init(this);
        isConnected = true;
    }

    private void toggleCamera(boolean enable) {
        if (mCamera.toggle(enable)) {
            flashState = enable;
            btnSwitch.setImageResource(enable ? R.drawable.ic_power_on : R.drawable.ic_power_off);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // yay, we got the permission -> turn on the light!
                    toggleCamera(!flashState);
                } else {
                    Toast.makeText(this, "Can not use flashlight without access to the camera.", Toast.LENGTH_SHORT).show();
                    // permission denied, boo!
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!flashState)
            close();
        else if (endWhenPaused) {
            stop();
        }
    }

    private void stop() {
        flashState = false;
        isConnected = false;
        mCamera.toggle(false);
        mCamera.release();
    }

    private void close() {
        flashState = false;
        isConnected = false;
        mCamera.release();
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

        if (!isConnected) {
            init();
        }

        if (flashState) {
            btnSwitch.setImageResource(R.drawable.ic_power_on);
        } else {
            btnSwitch.setImageResource(R.drawable.ic_power_off);
        }
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
                    ((MainActivity) getActivity()).goToNavigationItem(R.id.nav_help);
                }
            });

            return builder.create();
        }
    }
}
