package com.cuzor.rag.models
// this file is used to define the request body for the search endpoint (converting object format JSON ).
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(

    val query: String,

    val limit: Int = 20

)