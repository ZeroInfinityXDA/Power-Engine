package com.zeroinfinity.powerengine.objects

import android.content.Context
import android.graphics.drawable.Drawable
import com.zeroinfinity.powerengine.R

class App(var packageName: String, var appName: String, var profile: String?, var icon: Drawable?) {
    companion object Utils {
        fun getProfile(packageName: String, context: Context): String? =
            context.getSharedPreferences(
                context.getString(R.string.appProfileSharedPrefs),
                Context.MODE_PRIVATE
            )
                .getString(packageName, "balanced")

        fun setProfile(packageName: String, profile: String, context: Context) {
            context.getSharedPreferences(
                context.getString(R.string.appProfileSharedPrefs),
                Context.MODE_PRIVATE
            ).edit().putString(packageName, profile).apply()
        }
    }
}