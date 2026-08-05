package com.cuzor.rag.embeddings.onnx

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtSession

object ModelInspector {

    fun printOutputs(session: OrtSession) {

        println("========== MODEL OUTPUTS ==========")

        session.outputInfo.forEach { (name, info) ->
            println("$name -> ${info.info}")
        }

        println("===================================")
    }

    fun printInputs(session: OrtSession) {

        println("========== MODEL INPUTS ==========")

        session.inputInfo.forEach { (name, info) ->
            println("$name -> ${info.info}")
        }

        println("==================================")
    }
}