package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductAnalytics(
    val product: String,
    val queryCount: Long
)