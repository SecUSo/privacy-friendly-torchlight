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
import androidx.core.content.ContextCompat
import com.secuso.torchlight2.PFApplicationData
import com.secuso.torchlight2.camera.CameraMarshmallow
import com.secuso.torchlight2.camera.CameraNormal
import com.secuso.torchlight2.R
import com.secuso.torchlight2.camera.ICamera
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.permission.PFAPermission
import org.secuso.pfacore.ui.declareUsage

class MainActivity : BaseActivity() {
    private var flashState = false
    private var btnSwitch: ImageButton? = null
    private val closeOnPause by lazy {
        PFApplicationData.instance(this).closeOnPause
    }
    private var mCamera: ICamera? = null
    private var isConnected = false

    private lateinit var toggleCamera: () -> Unit
    override fun isActiveDrawerElement(element: DrawerElement) = element.name == ContextCompat.getString(this, R.string.nav_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        btnSwitch = findViewById<View>(R.id.btnSwitch) as ImageButton
        flashState = false

        // Use the camera permission to toggle the camera.
        // The permission request has to happen before the Activity has started,
        // otherwise a crash occurs due to being unable to add a lifecycle listener by declareUsage
        toggleCamera = PFAPermission.Camera.declareUsage(this) {
            onGranted = {
                if (mCamera!!.toggle(!flashState)) {
                    flashState = !flashState
                    btnSwitch!!.setImageResource(if (flashState) R.drawable.ic_power_on else R.drawable.ic_power_off)
                }
            }
            onDenied = {
                Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.permission_camera_for_flashlight_not_granted), Toast.LENGTH_SHORT).show()
            }
            showRationale = {
                rationaleTitle = ContextCompat.getString(this@MainActivity, R.string.permission_camera_for_flashlight_title)
                rationaleText = ContextCompat.getString(this@MainActivity, R.string.permission_camera_for_flashlight_description)
            }
        }

        findViewById<CheckBox>(R.id.cbPause).apply {
            isChecked = closeOnPause.value
            setOnClickListener {
                closeOnPause.value = !closeOnPause.value
            }
        }

        init()
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

        btnSwitch!!.setOnClickListener { toggleCamera() }
    }

    private fun setUpCamera() {
        mCamera!!.init(this)
        isConnected = true
    }

    override fun onStop() {
        super.onStop()
        if (!flashState) close()
        else if (closeOnPause.value) {
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
}
