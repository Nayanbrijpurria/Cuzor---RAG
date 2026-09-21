package com.cuzor.rag.models

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