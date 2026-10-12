package com.cuzor.rag.models
// This file defines the EmbeddingStatus enum class, which represents the status of an embedding operation.
// It has three possible values: PENDING, EMBEDDED, and FAILED. Each value is associated with an integer code.
// The companion object provides a method to retrieve an EmbeddingStatus from its code.

// Represents the status of an embedding operation.
//
// PENDING  = ticket is waiting to be embedded
// EMBEDDED = embedding was successfully generated
// FAILED   = embedding failed twice
// SKIPPED  = ticket does not have messages/conversation,
//            so embedding is not required

enum class EmbeddingStatus(
    val code: Int

) {

    PENDING(0),

    EMBEDDED(1),

    FAILED(2),

    SKIPPED(3);

    companion object {

        fun fromCode(code: Int): EmbeddingStatus {

            return entries.firstOrNull {
                it.code == code
            } ?: PENDING
        }
    }
}