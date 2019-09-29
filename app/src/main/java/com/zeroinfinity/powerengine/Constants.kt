package com.zeroinfinity.powerengine

const val version: String = "5.0.1b"
const val default_tag: String = "PowerEngine"
const val CHANNEL_ID: String = "profile_monitor"
const val APP_BACKUP: Int = 0
const val PROFILE_BACKUP: Int = 1
const val APP_RESTORE: Int = 2
const val PROFILE_RESTORE: Int = 3
val whitelist: List<String> = listOf(
    "android", "com.android.systemui", "com.fb.fluid", "com.google.android.gms",
    "com.google.android.play.games",
    "input",
    "keyboard",
    "oneplus.aod",
    "swiftkey",
    "com.google.android.googlequicksearchbox"
)
val GPUPaths: List<String> = listOf(
    "/sys/devices/platform/e82c0000.mali/devfreq/gpufreq",
    "/sys/class/ksgl/ksgl-3d0/devfreq",
    "/sys/devices/soc.0/qcom,kgsl-busmon.34/devfreq/qcom,kgsl-busmon.34",
    "/sys/devices/soc/b00000.qcom,kgsl-3d0/devfreq/b00000.qcom,kgsl-3d0",
    "/sys/devices/soc/5000000.qcom,kgsl-3d0/devfreq/5000000.qcom,kgsl-3d0",
    "/sys/devices/platform/soc/5000000.qcom,kgsl-3d0/devfreq/5000000.qcom,kgsl-3d0"
)