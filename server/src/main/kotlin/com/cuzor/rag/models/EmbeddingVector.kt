package com.cuzor.rag.models
// This file is used to store the embedding vector for a ticket.
// It is used to store the ticket ID and the embedding vector as a float array.

data class EmbeddingVector(

    val ticketId: String,

    val vector: FloatArray

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmbeddingVector

        if (ticketId != other.ticketId) return false
        if (!vector.contentEquals(other.vector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ticketId.hashCode()
        result = 31 * result + vector.contentHashCode()
        return result
    }
}