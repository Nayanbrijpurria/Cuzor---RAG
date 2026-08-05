package com.cuzor.rag.embeddings.onnx

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment

object TensorBuilder {

    fun createInputIds(

        ids: LongArray

    ): OnnxTensor {

        return OnnxTensor.createTensor(

            OrtEnvironment.getEnvironment(),

            arrayOf(ids)

        )

    }

    fun createAttentionMask(

        mask: LongArray

    ): OnnxTensor {

        return OnnxTensor.createTensor(

            OrtEnvironment.getEnvironment(),

            arrayOf(mask)

        )

    }

}