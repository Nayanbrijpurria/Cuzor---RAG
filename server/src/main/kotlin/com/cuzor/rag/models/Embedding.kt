package com.cuzor.rag.models

data class Embedding(

    val text: String,

    val vector: List<Float>

)