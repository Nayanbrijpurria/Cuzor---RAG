package com.cuzor.rag.embeddings.pooling

object MeanPooling {

    fun pool(
        embeddings: Array<Array<FloatArray>>
    ): FloatArray {

        require(embeddings.isNotEmpty()) {
            "Embeddings array cannot be empty."
        }

        require(embeddings[0].isNotEmpty()) {
            "Sequence cannot be empty."
        }

        val sequence = embeddings[0]

        val dimension = sequence[0].size

        val result = FloatArray(dimension)

        for (token in sequence) {

            for (i in token.indices) {

                result[i] += token[i]

            }

        }

        val tokenCount = sequence.size.toFloat()

        for (i in result.indices) {

            result[i] /= tokenCount

        }

        return result

    }

}