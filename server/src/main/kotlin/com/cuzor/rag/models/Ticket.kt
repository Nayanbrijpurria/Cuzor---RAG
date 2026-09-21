package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
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

    val conversation: String,

    // =====================================================
    // EMBEDDING STATUS
    //
    // 0 = PENDING
    // 1 = EMBEDDED
    // 2 = FAILED
    // =====================================================

    val embeddingVerified: Int = 0,

    // =====================================================
    // EMBEDDING ATTEMPTS
    //
    // 0 = never attempted
    // 1 = failed once
    // 2 = failed twice
    // =====================================================

    val embeddingAttempts: Int = 0
)