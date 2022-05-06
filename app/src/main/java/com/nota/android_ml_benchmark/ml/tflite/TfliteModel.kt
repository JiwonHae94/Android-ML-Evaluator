package com.jiwon.android_ml_benchmark.ml.tflite

import android.content.Context
import com.jiwon.android_ml_benchmark.benchmark.CpuMonitor
import com.jiwon.android_ml_benchmark.data.BenchmarkResult
import com.jiwon.android_ml_benchmark.ml.Networks
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class TfliteModel(
    val context : Context,
    val modelPath : String,
    val option : Interpreter.Options?
) : Networks {
    private val info = Runtime.getRuntime()
    private val totalMemorySize = info.totalMemory()

    private val tfliteInterpreter = Interpreter(
        FileUtil.loadMappedFile(context, modelPath),
        option
    )

    override fun runBenchmarkWOPostProcess(numWarmUp: Int, numInference: Int) : BenchmarkResult{
        val dummyInput = arrayOf(tfliteInterpreter.dummyInput().buffer)

        for(i in 0 until numWarmUp){

            tfliteInterpreter.runForMultipleInputsOutputs(
                dummyInput,
                tfliteInterpreter.dummyOutput(*IntArray(tfliteInterpreter.outputTensorCount){ it })
            )
        }

        val latencies = ArrayList<Long>()
        val cpuTimes = ArrayList<Double>()
        val cpuUsage = ArrayList<Double>()
        val memoryUsage = ArrayList<Long>()

        val (startCpuTime, startProcessTime) = CpuMonitor.startTrace()

        for(i in 0 until numInference){
            val memoryStart = (totalMemorySize - info.freeMemory()) / 1048576L
            tfliteInterpreter.runForMultipleInputsOutputs(
                dummyInput,
                tfliteInterpreter.dummyOutput(*IntArray(tfliteInterpreter.outputTensorCount){ it })
            )
            val latency = tfliteInterpreter.lastNativeInferenceDurationNanoseconds
            latencies.add(latency)

            val memoryEnd = (totalMemorySize - info.freeMemory()) / 1048576L
            memoryUsage.add(memoryEnd - memoryStart)
        }

        val (endCpuTime, endProcessTime) = CpuMonitor.endTrace()
        val cpuTimeDeltaSec = endCpuTime - startCpuTime
        val processTimeDeltaSec = endProcessTime - startProcessTime

        val relAvgUsagePercent = 100.0 * (cpuTimeDeltaSec / processTimeDeltaSec) / CpuMonitor.numCores
        cpuUsage.add(relAvgUsagePercent)
        cpuTimes.add(cpuTimeDeltaSec / numInference)

        val minTimeElapsed = latencies.minOrNull()
        val maxTimeElapsed = latencies.maxOrNull()
        val averageTimeElapsed = latencies.average().toLong()

        return BenchmarkResult(
            tag = modelPath,
            memoryUsed = memoryUsage.average(),
            deviceTemperature = floatArrayOf(),
            cpuTimes = cpuTimes.toTypedArray(),
            cpuUsage = cpuUsage.toTypedArray(),
            isGpuAvailable = CpuMonitor.isGpuAvailable,
            minTimeElapsed = minTimeElapsed ?: 0L,
            maxTimeElasped = maxTimeElapsed ?: 0L,
            averageTimeElapsed = averageTimeElapsed
        )
    }

    override fun runBenchmarkWithPostProcess(numWarmUp: Int, numInference: Int, postprocess: (HashMap<Int, Any>) -> Unit): BenchmarkResult {
        val dummyInput = arrayOf(tfliteInterpreter.dummyInput().buffer)
        val memoryUsed = (totalMemorySize - info.freeMemory()) / 1048576L

        for(i in 0 until numWarmUp){
            tfliteInterpreter.runForMultipleInputsOutputs(
                dummyInput,
                tfliteInterpreter.dummyOutput(*IntArray(tfliteInterpreter.outputTensorCount){ it })
            )
        }

        val timesElapsed = ArrayList<Long>()
        val cpuTimes = ArrayList<Double>()
        val cpuUsage = ArrayList<Double>()
        val memoryUsage = ArrayList<Long>()

        for(i in 0 until numInference){
            val memoryStart = (totalMemorySize - info.freeMemory()) / 1048576L

            val (startCpuTime, startProcessTime) = CpuMonitor.startTrace()
            val dummyOutput = tfliteInterpreter.dummyOutput(*IntArray(tfliteInterpreter.outputTensorCount){ it })

            tfliteInterpreter.runForMultipleInputsOutputs(
                dummyInput,
                dummyOutput
            )
            postprocess(dummyOutput)

            // FIXME needs to fix memory function
            val memoryEnd = (totalMemorySize - info.freeMemory()) / 1048576L
            memoryUsage.add(memoryEnd - memoryStart)


            val (endCpuTime, endProcessTime) = CpuMonitor.endTrace()
            val cpuTimeDelta = startCpuTime - endCpuTime
            val processTimeDelta = startProcessTime - endProcessTime

            val cpuUsagePerOperation = 100 * (cpuTimeDelta / processTimeDelta) / CpuMonitor.numCores
            cpuUsage.add(cpuUsagePerOperation)
            timesElapsed.add(tfliteInterpreter.lastNativeInferenceDurationNanoseconds / 1000000L) // calculate time in ms
        }

        val minTimeElapsed = timesElapsed.minOrNull()
        val maxTimeElapsed = timesElapsed.maxOrNull()
        val averageTimeElapsed = timesElapsed.average().toLong()

        return BenchmarkResult(
            tag = modelPath,
            memoryUsed = memoryUsage.average(),
            deviceTemperature = floatArrayOf(),
            cpuTimes = cpuTimes.toTypedArray(),
            cpuUsage = cpuUsage.toTypedArray(),
            isGpuAvailable = CpuMonitor.isGpuAvailable,
            minTimeElapsed = minTimeElapsed ?: 0L,
            maxTimeElasped = maxTimeElapsed ?: 0L,
            averageTimeElapsed = averageTimeElapsed
        )
    }

    private fun Interpreter.dummyInput() : TensorBuffer{
        val inputTensor = getInputTensor(0)
        return TensorBuffer.createFixedSize(inputTensor.shape(), inputTensor.dataType())
    }

    private fun Interpreter.dummyOutput(vararg tensors : Int) : HashMap<Int, Any>{
        val outTensors = HashMap<Int, Any>()
        tensors.indices.forEach { indx ->
            val outputTensor = getOutputTensor(indx)
            outTensors[indx] = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType()).buffer
        }
        return outTensors
    }
}