package com.zeroinfinity.powerengine.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.zeroinfinity.powerengine.R
import com.zeroinfinity.powerengine.Settings.showSystemApps
import com.zeroinfinity.powerengine.objects.App
import com.zeroinfinity.powerengine.whitelist
import java.util.*

class AppHelper(private val context: Context) {
    companion object {
        fun isWhitelist(packageName: String, appName: String): Boolean {
            if (whitelist.contains(packageName)) {
                return true
            } else {
                for (app in whitelist) {
                    if (app.contains(appName))
                        return true
                }
            }

            return false
        }

        fun getAppListName(ApkPackageName: String, context: Context): String {
            var name = "ERROR"
            val applicationInfo: ApplicationInfo
            val packageManager: PackageManager = context.packageManager

            try {
                applicationInfo = packageManager.getApplicationInfo(ApkPackageName, 0)
                if (applicationInfo != null) {
                    name = packageManager.getApplicationLabel(applicationInfo) as String
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return name
        }
    }

    fun getAllApps(search: String): ArrayList<App> {
        val packageNames: ArrayList<App> = ArrayList()
        val pm: PackageManager = context.packageManager
        val packages: List<ApplicationInfo> =
            pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (info in packages) {
            if (showSystemApps || !showSystemApps && !isSystemPackage(info)) {
                if (!isWhitelist(info.packageName, getAppListName(info.packageName, context))) {
                    if (search != "" && getAppListName(info.packageName, context).contains(
                            search,
                            true
                        ) || search == ""
                    ) {
                        packageNames.add(
                            App(
                                info.packageName,
                                getAppListName(info.packageName, context),
                                App.getProfile(info.packageName, context),
                                getAppIconByPackageName(info.packageName)
                            )
                        )
                    }
                }
            }
        }

        packageNames.sortWith(compareBy { it.appName })

        return ArrayList(packageNames)
    }

    private fun getAppIconByPackageName(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        }
    }

    private fun isSystemPackage(applicationInfo: ApplicationInfo): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}