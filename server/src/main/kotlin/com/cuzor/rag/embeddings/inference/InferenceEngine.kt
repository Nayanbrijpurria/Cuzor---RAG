package com.cuzor.rag.embeddings.inference

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtSession
import com.cuzor.rag.embeddings.OnnxSessionFactory
import com.cuzor.rag.models.TokenizedInput

class InferenceEngine {

    private val session =
        OnnxSessionFactory.session()

    fun infer(
        input: TokenizedInput
    ): OrtSession.Result {

        val inputIdsTensor =
            OnnxTensor.createTensor(

                OnnxSessionFactory.environment(),

                arrayOf(input.inputIds)

            )

        val attentionMaskTensor =
            OnnxTensor.createTensor(

                OnnxSessionFactory.environment(),

                arrayOf(input.attentionMask)

            )

        val inputs = mapOf(

            "input_ids" to inputIdsTensor,

            "attention_mask" to attentionMaskTensor

        )

        return session.run(inputs)

    }

}