package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(

    val ticketId: String,

    val similarity: Float

)