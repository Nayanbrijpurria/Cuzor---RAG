package com.cuzor.rag.similarity

import kotlin.math.sqrt

object CosineSimilarity {

    fun calculate(

        vectorA: FloatArray,

        vectorB: FloatArray

    ): Float {

        var dot = 0f

        var normA = 0f

        var normB = 0f

        for (i in vectorA.indices) {

            dot += vectorA[i] * vectorB[i]

            normA += vectorA[i] * vectorA[i]

            normB += vectorB[i] * vectorB[i]

        }

        return dot /

                (sqrt(normA) * sqrt(normB))

    }

}