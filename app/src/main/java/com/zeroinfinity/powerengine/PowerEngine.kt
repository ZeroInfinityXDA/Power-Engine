package com.zeroinfinity.powerengine

import android.content.Context
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import com.zeroinfinity.powerengine.Settings.notifications
import com.zeroinfinity.powerengine.helpers.CPUHelper
import com.zeroinfinity.powerengine.helpers.CPUHelper.clusters
import com.zeroinfinity.powerengine.helpers.CPUHelper.cpuPath
import com.zeroinfinity.powerengine.helpers.GPUHelper
import com.zeroinfinity.powerengine.helpers.GPUHelper.GPUFreqTable
import com.zeroinfinity.powerengine.helpers.GPUHelper.GPUPath
import com.zeroinfinity.powerengine.helpers.LoggingHelper

object PowerEngine {
    var previousProfile: String = ""
    var lastPackageName: String = ""

    fun appEngine(profile: String, packageName: String, context: Context) {
        if (profile != "whitelist") {
            val profilePrefs = Settings.getProfilePrefs(profile, context)
            val profileFreqs = CPUHelper.getCurrentProfileFreqs(profilePrefs)
            val governor = CPUHelper.getCurrentProfileGovernor(profilePrefs)

            Shell.su("echo 0:${profileFreqs[0]!![0]} > /sys/module/cpu_boost/parameters/input_boost_freq")
                .submit()
            Shell.su("echo ${profileFreqs[0]!![0]} > /sys/module/cpu_input_boost/parameters/input_boost_freq_lp")
                .submit()

            if (clusters.size > 1)
                Shell.su("echo ${profileFreqs[1]!![0]} > /sys/module/cpu_input_boost/parameters/input_boost_freq_hp")
                    .submit()

            for ((index, _) in clusters.withIndex()) {
                val maxFreqOutput = Shell.su("cat $cpuPath${clusters[index]}/scaling_max_freq")
                    .exec().out[0]

                if (!maxFreqOutput.contains("No such file or directory")) {
                    Shell.su("echo ${profileFreqs[index]!![1]} > $cpuPath${clusters[index]}/scaling_max_freq")
                        .submit()
                    Shell.su("echo ${profileFreqs[index]!![0]} > $cpuPath${clusters[index]}/scaling_min_freq")
                        .submit()
                    Shell.su("echo $governor > $cpuPath${clusters[index]}/scaling_governor")
                        .submit()
                } else {
                    CPUHelper.logError(
                        "Unable to set scaling_max_freq and scaling_min_freq " +
                                "for ${clusters[index]} - NOT FOUND!"
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.serviceError),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (GPUPath != "" && GPUFreqTable.isNotEmpty()) {
                val gpuMaxFreqOutput: MutableList<String> = Shell.su("cat $GPUPath/max_freq")
                    .exec().out
                val gpuFreqs = GPUHelper.getCurrentProfileFreqs(profilePrefs)

                if (!gpuMaxFreqOutput[0].contains("No such file or directory")) {
                    Shell.su("echo ${gpuFreqs[1]} > $GPUPath/max_freq")
                        .submit()
                    Shell.su("echo ${gpuFreqs[0]} > $GPUPath/min_freq")
                        .submit()
                } else {
                    LoggingHelper.writeToLogging(
                        "# Error: GPU Path was found but could not find min and max freqs",
                        true
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.serviceError),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        previousProfile = profile
        lastPackageName = packageName

        if (notifications)
            Notification.createNotification(context)
    }
}