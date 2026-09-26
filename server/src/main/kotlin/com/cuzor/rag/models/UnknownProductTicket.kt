package com.cuzor.rag.models

import kotlinx.serialization.Serializable
// this file is just to check what are the unknown products that are not being tracked.

@Serializable
data class UnknownProductTicket(
    val ticketId: String,
    val title: String,
    val conversation: String
)