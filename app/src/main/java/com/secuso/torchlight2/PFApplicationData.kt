package com.secuso.torchlight2

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.map
import org.secuso.pfacore.model.Theme
import org.secuso.pfacore.model.about.About
import org.secuso.pfacore.model.preferences.Preferable
import org.secuso.pfacore.model.preferences.settings.ISettingData
import org.secuso.pfacore.ui.PFData
import org.secuso.pfacore.ui.help.Help
import org.secuso.pfacore.ui.preferences.appPreferences
import org.secuso.pfacore.ui.preferences.settings.DeviceInformationOnErrorReport
import org.secuso.pfacore.ui.preferences.settings.PreferenceFirstTimeLaunch
import org.secuso.pfacore.ui.preferences.settings.SettingThemeSelector
import org.secuso.pfacore.ui.preferences.settings.appearance
import org.secuso.pfacore.ui.preferences.settings.general
import org.secuso.pfacore.ui.preferences.settings.preferenceFirstTimeLaunch
import org.secuso.pfacore.ui.preferences.settings.settingDeviceInformationOnErrorReport
import org.secuso.pfacore.ui.preferences.settings.settingThemeSelector
import org.secuso.pfacore.ui.tutorial.buildTutorial

class PFApplicationData private constructor(context: Context) {

    lateinit var theme: ISettingData<String>
        private set
    lateinit var firstTimeLaunch: Preferable<Boolean>
        private set
    lateinit var includeDeviceDataInReport: Preferable<Boolean>
        private set
    lateinit var closeOnPause: Preferable<Boolean>

    private val preferences = appPreferences(context) {
        preferences {
            firstTimeLaunch = preferenceFirstTimeLaunch
            closeOnPause = preference {
                key = "closeOnPause"
                default = false
                backup = false
            }
        }
        settings {
            general {
                theme = settingThemeSelector
                includeDeviceDataInReport = settingDeviceInformationOnErrorReport
            }
        }
    }

    private val help = Help.build(context) {
        item {
            title { resource(R.string.help) }
            description { resource(R.string.help_intro) }
        }
        item {
            title { resource(R.string.help_privacy_heading) }
            description { resource(R.string.help_permissions_description) }
        }
    }

    private val about = About(
        name = context.resources.getString(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        authors = context.resources.getString(R.string.about_author_names),
        repo = context.resources.getString(R.string.github)
    )

    private val tutorial = buildTutorial {
        stage {
            title = context.getString(R.string.app_name_long)
            images = listOf(R.mipmap.ic_splash)
            description = context.getString(R.string.description)
        }
    }

    val data = PFData(
        about = about,
        help = help,
        settings = preferences.settings,
        tutorial = tutorial,
        theme = theme.state.map { Theme.valueOf(it) },
        firstLaunch = firstTimeLaunch,
        includeDeviceDataInReport = includeDeviceDataInReport,
    )

    companion object {
        private var _instance: PFApplicationData? = null
        fun instance(context: Context): PFApplicationData {
            if (_instance == null) {
                _instance = PFApplicationData(context)
            }
            return _instance!!
        }

    }
}


