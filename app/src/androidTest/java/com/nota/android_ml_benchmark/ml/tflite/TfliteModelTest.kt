package com.nota.android_ml_benchmark.ml.tflite

import androidx.test.platform.app.InstrumentationRegistry
import com.jiwon.android_ml_benchmark.benchmark.CpuMonitor
import com.jiwon.android_ml_benchmark.ml.tflite.TfliteModel
import org.junit.Before
import org.junit.Test
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate

class TfliteModelTest{

    @Test
    fun testBenchmark(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        //val gpuDelegate = GpuDelegate()
        val benchmarkResult = TfliteModel(appContext, "ssd_mobilenet_v1_1_metadata_1.tflite", Interpreter.Options().apply {
            setUseXNNPACK(true)
            //addDelegate(gpuDelegate)
            setNumThreads(4)
        }).runBenchmarkWOPostProcess(30, 10)

        println(benchmarkResult)
        //gpuDelegate.close()
    }
}