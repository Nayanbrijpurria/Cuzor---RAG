package com.cuzor.rag.models
// this file defines the data class for a retrieved ticket,
// which includes various properties such as ticket ID, creation date, state, category, urgency, customer information, message count, title, description, resolution, conversation, and similarity score.
// The class is annotated with @Serializable to enable serialization and deserialization of instances of this class.
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