package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class RetrievalResponse(

    val highConfidence: List<RetrievedTicket>,

    val lowConfidence: List<RetrievedTicket>
)