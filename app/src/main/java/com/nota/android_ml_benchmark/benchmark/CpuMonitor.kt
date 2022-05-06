package com.jiwon.android_ml_benchmark.benchmark

import android.content.Context
import android.os.SystemClock
import android.system.Os
import android.system.OsConstants
import android.util.Log
import org.tensorflow.lite.gpu.CompatibilityList
import java.io.File
import java.lang.StringBuilder

object CpuMonitor {
    private val TAG = CpuMonitor::class.java.simpleName
    val isGpuAvailable = false

    val clockSpeedHz = Os.sysconf(OsConstants._SC_CLK_TCK)
    val numCores = Os.sysconf(OsConstants._SC_NPROCESSORS_CONF)

    fun startTrace() : Pair<Double, Double>{
        val upTimeSec = SystemClock.elapsedRealtime() / 1000.0 // time since device booted

        val statFile = File("/proc/${android.os.Process.myPid()}/stat")
        val builder = StringBuilder()
        statFile.bufferedReader().forEachLine {
            builder.append("$it ")
        }

        val values = builder.toString().split(" ")
        // amount of time that this process has been scheduled in user mode, measured in clock ticks
        val utime = values[INDEX_UTIME].toDouble()
        // amount of time that this process has been scheduled in kernel mode, measured in clock ticks
        val stime = values[INDEX_STIME].toDouble()
        // amount of time that this process’ waited-for children have been scheduled in user mode, measured in clock tick
        val cutime = values[INDEX_CUTIME].toDouble()
        // amount of time that this process’ waited-for children have been scheduled in kernel mode, measured in clock ticks
        val cstime = values[INDEX_CSTIME].toDouble()
        // the time the process started after system boot, measured in clock ticks
        val starttime = values[INDEX_STARTTIME].toDouble()

        // cpu time in seconds
        val cpuTimeSec : Double = (utime + stime + cutime + cstime) / clockSpeedHz.toDouble()
        val processTimeSec : Double = upTimeSec - (starttime / clockSpeedHz.toDouble())

        return Pair(cpuTimeSec, processTimeSec)
    }

    fun endTrace() : Pair<Double, Double>{
        val upTimeSec = SystemClock.elapsedRealtime() / 1000.0 // time since device booted
        val statFile = File("/proc/${android.os.Process.myPid()}/stat")
        val builder = StringBuilder()
        statFile.bufferedReader().forEachLine {
            builder.append("$it ")
        }

        val values = builder.toString().split(" ")
        // amount of time that this process has been scheduled in user mode, measured in clock ticks
        val utime = values[INDEX_UTIME].toDouble()
        // amount of time that this process has been scheduled in kernel mode, measured in clock ticks
        val stime = values[INDEX_STIME].toDouble()
        // amount of time that this process’ waited-for children have been scheduled in user mode, measured in clock tick
        val cutime = values[INDEX_CUTIME].toDouble()
        // amount of time that this process’ waited-for children have been scheduled in kernel mode, measured in clock ticks
        val cstime = values[INDEX_CSTIME].toDouble()
        // the time the process started after system boot, measured in clock ticks
        val starttime = values[INDEX_STARTTIME].toDouble()

        val cpuTimeSec : Double = (utime + stime + cutime + cstime) / clockSpeedHz.toDouble()
        val processTimeSec : Double = upTimeSec - (starttime / clockSpeedHz.toDouble())

        return Pair(cpuTimeSec, processTimeSec)
    }

    private const val INDEX_UTIME = 13
    private const val INDEX_STIME = 14
    private const val INDEX_CUTIME = 15
    private const val INDEX_CSTIME = 16
    private const val INDEX_STARTTIME = 21

}