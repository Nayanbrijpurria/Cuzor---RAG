package com.cuzor.rag.embeddings

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.cuzor.rag.embeddings.onnx.ModelInspector
import java.nio.file.Paths

object OnnxSessionFactory {

    private val environment = OrtEnvironment.getEnvironment()

    private val session: OrtSession by lazy {

        val options = OrtSession.SessionOptions()

        options.setOptimizationLevel(
            OrtSession.SessionOptions.OptLevel.ALL_OPT
        )

        val modelPath = OnnxSessionFactory::class.java.classLoader
            .getResource("models/bge-m3/model_int8.onnx")
            ?.let { Paths.get(it.toURI()).toString() }
            ?: throw RuntimeException("Model file not found in resources: models/bge-m3/model_int8.onnx")

        val session = environment.createSession(
            modelPath,
            options
        )

        ModelInspector.printOutputs(session)

        session
    }

    fun session(): OrtSession = session

    fun environment(): OrtEnvironment = environment
}