package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class RetrievedTicket(

    val ticketId: String,

    val title: String,

    val description: String,

    val resolution: String,

    val conversation: String,

    val similarity: Float

)