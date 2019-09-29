package com.zeroinfinity.powerengine.helpers

import android.content.SharedPreferences
import com.topjohnwu.superuser.Shell
import com.zeroinfinity.powerengine.GPUPaths

object GPUHelper {
    var GPUPath: String = ""
    var GPUFreqTable: MutableList<String> = mutableListOf()

    init {
        for (path in GPUPaths) {
            val out = Shell.su("ls $path").exec().out
            if (out.size > 0 && !out[0].contains("No such file or directory")) {
                GPUPath = path
                break
            }
        }

        if (GPUPath != "")
            getFreqs()
    }

    private fun getFreqs() {
        val out: String = Shell.su("cat $GPUPath/available_frequencies").exec().out[0]

        if (!out.contains("No such file or directory")) {
            GPUFreqTable = out.split(" ").toMutableList()
        } else {
            LoggingHelper.writeToLogging(
                "# Error: GPU path was found but could not find the frequency table",
                true
            )
        }

        if (GPUFreqTable.isNotEmpty()) {
            GPUFreqTable.remove("")
            if (GPUFreqTable.first().toLong() < GPUFreqTable.last().toLong())
                GPUFreqTable.reverse()
        }
    }

    fun setMaxFreq(sharedPreferences: SharedPreferences, freq: String) {
        sharedPreferences.edit().putString("gpu_max", freq).apply()
    }

    fun setMinFreq(sharedPreferences: SharedPreferences, freq: String) {
        sharedPreferences.edit().putString("gpu_min", freq).apply()
    }

    fun getCurrentProfileFreqs(sharedPreferences: SharedPreferences): MutableList<String> {
        val minMax: MutableList<String> = mutableListOf()
        minMax.add(sharedPreferences.getString("gpu_min", GPUFreqTable.last())!!)
        minMax.add(sharedPreferences.getString("gpu_max", GPUFreqTable.first())!!)

        return minMax
    }
}