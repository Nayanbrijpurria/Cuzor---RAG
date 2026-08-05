package com.cuzor.rag.embeddings

import com.cuzor.rag.models.TokenizedInput
import com.cuzor.rag.service.tokenizer.TokenizerClient

class Tokenizer {

    suspend fun tokenize(
        text: String
    ): TokenizedInput {

        val response =
            TokenizerClient.tokenize(text)

        return TokenizedInput(

            inputIds =
                response.input_ids.toLongArray(),

            attentionMask =
                response.attention_mask.toLongArray()

        )

    }

}