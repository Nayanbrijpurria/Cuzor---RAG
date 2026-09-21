package com.cuzor.rag.embeddings.normalization

import kotlin.math.sqrt

object L2Normalizer {

    fun normalize(

        vector: FloatArray

    ): FloatArray {

        var sum = 0f

        for (v in vector) {
            sum += v * v
        }

        val norm =
            sqrt(sum)

        for (i in vector.indices) {

            vector[i] /= norm

        }

        return vector

    }

}