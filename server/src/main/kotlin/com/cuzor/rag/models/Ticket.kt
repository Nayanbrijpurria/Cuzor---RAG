package com.cuzor.rag.models

data class Ticket(

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

    val conversation: String

)