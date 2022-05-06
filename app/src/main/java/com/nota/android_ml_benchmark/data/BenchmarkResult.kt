package com.jiwon.android_ml_benchmark.data

import android.system.Os
import android.system.OsConstants
import java.lang.StringBuilder

/**
 * https://eng.lyft.com/monitoring-cpu-performance-of-lyfts-android-applications-4e36fafffe12
 */
data class BenchmarkResult(
    val tag : String,
    val numProcessors: Long = Os.sysconf(OsConstants._SC_NPROCESSORS_CONF),
    val clockSpeed: Long = Os.sysconf(OsConstants._SC_CLK_TCK),
    val memoryUsed: Long,
    val isGpuAvailable : Boolean,
    val deviceTemperature: FloatArray,
    val cpuTimes: Array<Double>,
    val cpuUsage : Array<Double>,
    val minTimeElapsed: Long,
    val maxTimeElasped: Long,
    val averageTimeElapsed: Long
) {
    override fun toString(): String {
        return StringBuilder()
            .append("Benchmark - $tag\n")
            .append("no. processors     : $numProcessors\n")
            .append("clock speed        : $clockSpeed Hz\n")
            .append("cpu max time       : ${cpuTimes.maxOrNull()}s\n")
            .append("cpu min time       : ${cpuTimes.minOrNull()}s\n")
            .append("cpu average time   : ${"%.3f".format(cpuTimes.average())}s\n")
            .append("cpu usage max      : ${"%.3f".format(cpuUsage.maxOrNull())}%\n")
            .append("cpu usage min      : ${"%.3f".format(cpuUsage.minOrNull())}%\n")
            .append("cpu usage average  : ${"%.3f".format(cpuUsage.average())}%\n")
            .append("gpu availability   : ${isGpuAvailable}")
            .append("memory used        : $memoryUsed mb\n")
            .append("minTimeElapsed     : $minTimeElapsed ms\n")
            .append("maxTimeElasped     : $maxTimeElasped ms\n")
            .append("averageTimeElapsed : $averageTimeElapsed ms\n")
            .toString()
    }
}