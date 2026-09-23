package com.cuzor.rag.models
// This file defines the EmbeddingStatus enum class, which represents the status of an embedding operation.
// It has three possible values: PENDING, EMBEDDED, and FAILED. Each value is associated with an integer code.
// The companion object provides a method to retrieve an EmbeddingStatus from its code.

enum class EmbeddingStatus(
    val code: Int
) {
    PENDING(0),

    EMBEDDED(1),

    FAILED(2);

    companion object {

        fun fromCode(code: Int): EmbeddingStatus {

            return entries.firstOrNull {
                it.code == code
            } ?: PENDING
        }
    }
}