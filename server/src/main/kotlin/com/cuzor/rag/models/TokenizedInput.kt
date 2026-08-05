package com.cuzor.rag.models

data class TokenizedInput(

    val inputIds: LongArray,

    val attentionMask: LongArray

)