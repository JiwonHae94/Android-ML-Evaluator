package com.jiwon.android_ml_benchmark.ml

import com.jiwon.android_ml_benchmark.data.BenchmarkResult

interface Networks {
    // start inferencing
    fun runBenchmarkWOPostProcess(numWarmUp : Int, numInference : Int) : BenchmarkResult

    fun runBenchmarkWithPostProcess(numWarmUp: Int, numInference: Int, postprocess : (HashMap<Int, Any>) -> Unit) : BenchmarkResult
}