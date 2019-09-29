package com.zeroinfinity.powerengine

import android.app.Application
import android.util.Log
import com.topjohnwu.superuser.Shell
import com.zeroinfinity.powerengine.Settings.chargingMode
import com.zeroinfinity.powerengine.Settings.darkTheme
import com.zeroinfinity.powerengine.Settings.notifications
import com.zeroinfinity.powerengine.Settings.showSystemApps
import com.zeroinfinity.powerengine.fragments.MoreFragment
import com.zeroinfinity.powerengine.helpers.CPUHelper
import com.zeroinfinity.powerengine.helpers.GPUHelper
import com.zeroinfinity.powerengine.helpers.LoggingHelper
import net.danlew.android.joda.JodaTimeAndroid

class PowerEngineApp : Application() {
    companion object {
        init {
            Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
            Shell.Config.verboseLogging(BuildConfig.DEBUG)
            Shell.Config.setTimeout(10)
            Log.d(default_tag, "Shell initialized!")
        }
    }

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        LoggingHelper.getLogFile(applicationContext)
        if (Shell.rootAccess()) {
            CPUHelper
            GPUHelper
        }

        darkTheme =
            MoreFragment.readBooleanPreference(getString(R.string.dark_theme), applicationContext)
        notifications =
            MoreFragment.readBooleanPreference(
                getString(R.string.notifications),
                applicationContext
            )
        showSystemApps =
            MoreFragment.readBooleanPreference(getString(R.string.system_app), applicationContext)
        chargingMode =
            MoreFragment.readBooleanPreference(
                getString(R.string.charging_mode),
                applicationContext
            )
    }
}