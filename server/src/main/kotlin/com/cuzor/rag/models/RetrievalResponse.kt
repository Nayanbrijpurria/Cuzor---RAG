package com.cuzor.rag.models

// this file is used to define the response of the retrieval process,
// which contains two lists of RetrievedTicket objects: one for high confidence matches and one for low confidence matches.

import kotlinx.serialization.Serializable

@Serializable
data class RetrievalResponse(

    val highConfidence: List<RetrievedTicket>,

    val lowConfidence: List<RetrievedTicket>
)