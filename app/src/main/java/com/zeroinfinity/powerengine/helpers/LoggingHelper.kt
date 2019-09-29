package com.zeroinfinity.powerengine.helpers

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import com.zeroinfinity.powerengine.default_tag
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

object LoggingHelper {
    private lateinit var logFile: File

    fun getLogFile(context: Context) {
        logFile = File(context.getExternalFilesDir(null), "power_engine.log")
    }

    fun produceLogcat(context: Context) {
        GlobalScope.launch {
            Shell.sh("logcat -d SHELL_IN:D SHELLOUT:D *:S > ${context.getExternalFilesDir(null)}/power_engine_logcat.txt")
                .submit()
        }
    }

    fun writeToLogging(logData: String, append: Boolean) {
        var data: String = logData

        if (!logFile.exists() || logFile.length() / 1024 == 512.toLong()) {
            logFile.createNewFile()
            writeToLogging(
                "# Power Engine Error Log\n# Started on: " + LocalDateTime.now().toDate() + "\n",
                false
            )
        } else {
            data = "\n" + data
        }

        try {
            val outputStreamWriter = OutputStreamWriter(FileOutputStream(logFile, append))
            outputStreamWriter.write(data)
            outputStreamWriter.flush()
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(
                default_tag,
                "Could not write to logging file! Perhaps storage permissions are denied?"
            )
        }
    }
}