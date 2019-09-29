package com.zeroinfinity.powerengine

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import com.zeroinfinity.powerengine.Notification.cancelNotification
import com.zeroinfinity.powerengine.PowerEngine.previousProfile
import com.zeroinfinity.powerengine.Settings.chargingMode
import com.zeroinfinity.powerengine.helpers.AppHelper
import com.zeroinfinity.powerengine.helpers.CPUHelper.freqTables
import com.zeroinfinity.powerengine.objects.App
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PowerEngineService : AccessibilityService() {
    private var isChargingAlreadyEnabled = false

    override fun onCreate() {
        super.onCreate()
        if (Shell.rootAccess()) {
            Shell.su("sleep 4; stop perfd; stop perf-hal-1-0").submit()
            createNotificationChannel()

            if (checkBatteryOptimized()) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                Toast.makeText(
                    applicationContext,
                    getString(R.string.batteryopt),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.no_root_detected),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onInterrupt() {
        cancelNotification(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelNotification(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (Shell.rootAccess() || freqTables.isNotEmpty()) {
            if (event!!.packageName != null) {
                val packageName: String = event.packageName.toString()
                val appName = AppHelper.getAppListName(packageName, applicationContext)
                val appProfile: String
                val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = applicationContext.registerReceiver(null, iFilter)
                var isCharging = false

                if (chargingMode) {
                    if (batteryStatus != null) {
                        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL
                    }
                }

                if (isCharging) {
                    if (!isChargingAlreadyEnabled) {
                        isChargingAlreadyEnabled = true
                        PowerEngine.appEngine("performance", "Charging Mode", applicationContext)
                    }
                } else {
                    if (!AppHelper.isWhitelist(packageName, appName)) {
                        if (isChargingAlreadyEnabled) {
                            previousProfile = ""
                            isChargingAlreadyEnabled = false
                        }

                        appProfile = App.getProfile(packageName, applicationContext)!!

                        GlobalScope.launch {
                            PowerEngine.appEngine(appProfile, packageName, applicationContext)
                        }
                    }
                }
            }
        } else {
            Toast.makeText(applicationContext, getString(R.string.cpu_error), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkBatteryOptimized(): Boolean {
        val pwrm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = applicationContext.packageName
        return !pwrm.isIgnoringBatteryOptimizations(name)
    }
}