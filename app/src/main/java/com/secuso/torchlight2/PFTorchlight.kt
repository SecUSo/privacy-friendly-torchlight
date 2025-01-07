package com.secuso.torchlight2

import android.util.Log
import androidx.work.Configuration
import com.secuso.torchlight2.ui.MainActivity
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData

class PFTorchlight: PFApplication() {
    override val name: String
        get() = getString(R.string.app_name)

    override val data: PFData
        get() = PFApplicationData.instance(baseContext).data
    override val mainActivity = MainActivity::class.java

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}