package com.cuzor.rag.embeddings
import com.cuzor.rag.embeddings.inference.InferenceEngine
import com.cuzor.rag.embeddings.pooling.MeanPooling
import com.cuzor.rag.embeddings.normalization.L2Normalizer

class BgeEmbeddingModel : EmbeddingModel {

    private val tokenizer = Tokenizer()
    private val inferenceEngine =
        InferenceEngine()
    override suspend fun generateEmbedding(

        text: String

    ): FloatArray {

        println("Generating Embedding")

        println(text)

        val encoded = tokenizer.tokenize(text)

        println("Token Count : ${encoded.inputIds.size}")
        val result = inferenceEngine.infer(encoded)

        val output = result.get(0)?.value
            ?: throw RuntimeException("No output from ONNX model.")

        println("Output type = ${output::class.qualifiedName}")

        val embeddings = output as Array<Array<FloatArray>>

        val pooled = MeanPooling.pool(embeddings)
        val normalized = L2Normalizer.normalize(pooled)
         
        return normalized
    }

}
