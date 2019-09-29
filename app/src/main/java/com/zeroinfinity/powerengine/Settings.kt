package com.zeroinfinity.powerengine

import android.content.Context
import android.content.SharedPreferences

object Settings {
    var showSystemApps = false
    var notifications = false
    var darkTheme = false
    var chargingMode = false

    fun getProfilePrefs(profile: String, context: Context): SharedPreferences =
        when (profile) {
            "battery" -> context.getSharedPreferences(
                context.getString(R.string.profileBatteryPrefs),
                Context.MODE_PRIVATE
            )
            "balanced" -> context.getSharedPreferences(
                context.getString(R.string.profileBalancedPrefs),
                Context.MODE_PRIVATE
            )
            "performance" -> context.getSharedPreferences(
                context.getString(R.string.profilePerformancePrefs),
                Context.MODE_PRIVATE
            )
            else -> context.getSharedPreferences(
                context.getString(R.string.profileBatteryPrefs),
                Context.MODE_PRIVATE
            )
        }

    fun getAllProfiles(context: Context): Map<String, SharedPreferences> {
        val out: MutableMap<String, SharedPreferences> = mutableMapOf()
        out["battery"] = getProfilePrefs("battery", context)
        out["balanced"] = getProfilePrefs("balanced", context)
        out["performance"] = getProfilePrefs("performance", context)
        return out
    }
}