package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(

    val query: String,

    val limit: Int = 20

)