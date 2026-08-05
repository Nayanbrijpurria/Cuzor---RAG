package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(

    val query: String

)