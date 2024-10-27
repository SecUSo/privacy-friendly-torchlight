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
 * along with Privacy Friendly Torchlight. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.secuso.torchlight2.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.secuso.torchlight2.R
import com.secuso.torchlight2.camera.CameraMarshmallow
import com.secuso.torchlight2.camera.CameraNormal
import com.secuso.torchlight2.camera.ICamera

class MainActivity : BaseActivity() {
    private var flashState = false
    private var btnSwitch: ImageButton? = null
    private var endWhenPaused = false
    private lateinit var preferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private var mCamera: ICamera? = null
    private var isConnected = false
    private val thisActivity: Activity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        btnSwitch = findViewById<View>(R.id.btnSwitch) as ImageButton
        preferences = this.getPreferences(MODE_PRIVATE)
        prefEditor = preferences.edit()
        flashState = false
        endWhenPaused = preferences.getBoolean("closeOnPause", false)

        findViewById<CheckBox>(R.id.cbPause).apply {
            isChecked = endWhenPaused
            setOnClickListener {
                endWhenPaused = !endWhenPaused
                prefEditor.putBoolean("closeOnPause", endWhenPaused)
                prefEditor.commit()
            }
        }

        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putString("firstShow", "")
            .apply()
        getSharedPreferences("firstShow", MODE_PRIVATE).apply {
            if (getBoolean("isFirstRun", true)) {
                val welcomeDialog = WelcomeDialog()
                welcomeDialog.show(fragmentManager, "WelcomeDialog")

                edit()
                    .putBoolean("isFirstRun", false)
                    .commit()
            }
        }


        init()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun init() {
        val pm = packageManager

        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e("err", "Device has no camera!")
            Toast.makeText(this, R.string.no_flash, Toast.LENGTH_LONG).show()
            btnSwitch!!.isEnabled = false
            return
        }

        // Check Android Version
        mCamera = if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraMarshmallow()
        } else {
            CameraNormal()
        }

        // set up camera
        setUpCamera()

        btnSwitch!!.setOnClickListener(View.OnClickListener { // can we have permissions that are revoked?
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // check if we have the permission we need -> if not request it and turn on the light afterwards
                if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(thisActivity, arrayOf(Manifest.permission.CAMERA), 0)
                    return@OnClickListener
                }
            }
            toggleCamera(!flashState)
        })
    }

    private fun setUpCamera() {
        mCamera!!.init(this)
        isConnected = true
    }

    private fun toggleCamera(enable: Boolean) {
        if (mCamera!!.toggle(enable)) {
            flashState = enable
            btnSwitch!!.setImageResource(if (enable) R.drawable.ic_power_on else R.drawable.ic_power_off)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // yay, we got the permission -> turn on the light!
                    toggleCamera(!flashState)
                } else {
                    Toast.makeText(this, "Can not use flashlight without access to the camera.", Toast.LENGTH_SHORT).show()
                    // permission denied, boo!
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!flashState) close()
        else if (endWhenPaused) {
            stop()
        }
    }

    private fun stop() {
        flashState = false
        isConnected = false
        mCamera!!.toggle(false)
        mCamera!!.release()
    }

    private fun close() {
        flashState = false
        isConnected = false
        mCamera!!.release()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()

        if (!isConnected) {
            init()
        }

        if (flashState) {
            btnSwitch!!.setImageResource(R.drawable.ic_power_on)
        } else {
            btnSwitch!!.setImageResource(R.drawable.ic_power_off)
        }
    }

    override fun getNavigationDrawerID(): Int {
        return 0
    }


    class WelcomeDialog : DialogFragment() {
        override fun onAttach(activity: Activity) {
            super.onAttach(activity)
        }

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
            val i = activity.layoutInflater
            val builder = AlertDialog.Builder(activity)
            builder.setView(i.inflate(R.layout.welcome_dialog, null))
            builder.setIcon(R.mipmap.ic_launcher)
            builder.setTitle(activity.getString(R.string.welcome))
            builder.setPositiveButton(activity.getString(R.string.okay), null)
            builder.setNegativeButton(
                activity.getString(R.string.viewhelp)
            ) { dialog, which -> (activity as MainActivity).goToNavigationItem(R.id.nav_help) }

            return builder.create()
        }
    }
}
