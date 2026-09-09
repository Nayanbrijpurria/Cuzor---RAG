package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class RetrievedTicket(

    val ticketId: String,

    val created: String,

    val closedAt: String,

    val state: String,

    val category: String,

    val urgency: Int,

    val customerName: String,

    val customerPhone: String,

    val messageCount: Int,

    val title: String,

    val description: String,

    val resolution: String,

    val conversation: String,

    val similarity: Float

)