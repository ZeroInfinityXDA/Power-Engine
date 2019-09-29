package com.zeroinfinity.powerengine.helpers

import android.content.SharedPreferences
import com.topjohnwu.superuser.Shell

object CPUHelper {
    var clusters: MutableList<String> = mutableListOf()
    var freqTables: MutableMap<Int, MutableList<String>> = mutableMapOf()
    var governorList: MutableList<String> = mutableListOf()
    var cpuPath: String = ""

    init {
        getNumberOfClusters()
        getFreqs()
        getGovernors()
    }

    private fun getNumberOfClusters() {
        val checkPolicy: String =
            Shell.su("ls /sys/devices/system/cpu/cpufreq/policy0").exec().out[0]
        val arch: String = Shell.su("grep \"CPU architecture\" /proc/cpuinfo\n").exec().out[0]

        if (checkPolicy.contains("No such file or directory")) {
            cpuPath = "/sys/devices/system/cpu/"
            clusters.add("cpu0/cpufreq")

            if (arch.contains("8")) {
                val noOfCores: Int =
                    Shell.su("grep -c ^processor /proc/cpuinfo").exec().out[0].toInt()

                for (i in 1 until noOfCores) {
                    val checkCPU: String = Shell.su("file ${cpuPath}cpu$i/cpufreq").exec().out[0]
                    if (checkCPU.contains("directory"))
                        clusters.add("cpu$i/cpufreq")
                }
            }
        } else {
            cpuPath = "/sys/devices/system/cpu/cpufreq/"
            val output = Shell.su("ls $cpuPath").exec().out

            for (cluster in output) {
                if (cluster.contains("policy"))
                    clusters.add(cluster)
            }
        }
    }

    private fun getFreqs() {
        val checkFreqTable: String =
            Shell.su("cat $cpuPath${clusters[0]}/scaling_available_frequencies")
                .exec().out[0]

        for (i in 0 until clusters.size) {
            var freqTable: MutableList<String> = mutableListOf()

            if (!checkFreqTable.contains("No such file or directory")) {
                freqTable = Shell.su(
                    "cat $cpuPath${clusters[i]}/scaling_available_frequencies"
                ).exec().out[0].split(" ").toMutableList()
            } else {
                val checkSamsung: String = Shell
                    .su("cat /sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster0_freq_table").exec().out[0]

                if (checkSamsung.contains("No such file or directory")) {
                    logError("Could not find location of frequency tables!")
                } else {
                    freqTable = Shell.su(
                        "cat /sys/devices/system/cpu/cpufreq/mp-cpufreq/cluster${i}_freq_table"
                    ).exec().out[0].split(" ").toMutableList()
                }
            }

            if (freqTable.isNotEmpty()) {
                freqTable.remove("")

                if (freqTable.first().toLong() < freqTable.last().toLong())
                    freqTable.reverse()
            }

            freqTables[i] = freqTable
        }
    }

    private fun getGovernors() {
        val checkGovernors: String =
            Shell.su("cat $cpuPath${clusters[0]}/scaling_available_governors").exec().out[0]

        if (!checkGovernors.contains("No such file or directory")) {
            governorList =
                Shell.su("cat $cpuPath${clusters[0]}/scaling_available_governors").exec().out[0]
                    .split(" ").toMutableList()
            governorList.remove("")
        } else {
            logError("Could not find location of the list of governors!")
        }
    }

    fun setMaxFreq(sharedPreferences: SharedPreferences, freq: String, cluster: String) {
        sharedPreferences.edit().putString(cluster + "_max", freq).apply()
    }

    fun setMinFreq(sharedPreferences: SharedPreferences, freq: String, cluster: String) {
        sharedPreferences.edit().putString(cluster + "_min", freq).apply()
    }

    fun setGovernor(sharedPreferences: SharedPreferences, governor: String) {
        sharedPreferences.edit().putString("governor", governor).apply()
    }

    fun getCurrentProfileFreqs(sharedPreferences: SharedPreferences): MutableMap<Int, MutableList<String?>> {
        val minMaxFreqs: MutableMap<Int, MutableList<String?>> = mutableMapOf()

        for (i in 0 until clusters.size) {
            val minMax: MutableList<String?> = mutableListOf()
            minMax.add(sharedPreferences.getString("${clusters[i]}_min", freqTables[i]?.last()))
            minMax.add(sharedPreferences.getString("${clusters[i]}_max", freqTables[i]?.first()))

            minMaxFreqs[i] = minMax
        }

        return minMaxFreqs
    }

    fun getCurrentProfileGovernor(sharedPreferences: SharedPreferences): String =
        sharedPreferences.getString(
            "governor", Shell.su(
                "cat $cpuPath${clusters[0]}/scaling_governor"
            ).exec().out[0]
        )!!

    fun formatFreq(freq: String): String {
        if (freq.length == 7)
            return freq.substring(0, 4) + " MHz"
        else
            return freq.substring(0, 3) + " MHz"
    }

    fun logError(errorOutput: String) {
        val errorOutputs: MutableList<String> = mutableListOf()

        LoggingHelper.writeToLogging("# Error:", true)
        LoggingHelper.writeToLogging(errorOutput, true)

        Shell.su("ls /sys/devices/system/cpu/cpufreq").to(errorOutputs).exec()
        LoggingHelper.writeToLogging("# Contents of /sys/devices/system/cpu/cpufreq:", true)

        for (output in errorOutputs) {
            LoggingHelper.writeToLogging(output, true)
        }

        errorOutputs.clear()
        Shell.su("ls /sys/devices/system/cpu").to(errorOutputs).exec()
        LoggingHelper.writeToLogging("# Contents of /sys/devices/system/cpu:", true)

        for (output in errorOutputs) {
            LoggingHelper.writeToLogging(output, true)
        }

        errorOutputs.clear()
        Shell.su("ls /sys/devices/system/cpu/cpu0/cpufreq").to(errorOutputs).exec()
        LoggingHelper.writeToLogging("# Contents of /sys/devices/system/cpu0/cpufreq:", true)

        for (output in errorOutputs) {
            LoggingHelper.writeToLogging(output, true)
        }

        errorOutputs.clear()
        Shell.su("ls /sys/devices/system/cpu/cpufreq/mp-cpufreq").to(errorOutputs).exec()
        LoggingHelper.writeToLogging(
            "# Contents of /sys/devices/system/cpu/cpufreq/mp-cpufreq:",
            true
        )

        for (output in errorOutputs) {
            LoggingHelper.writeToLogging(output, true)
        }
    }
}