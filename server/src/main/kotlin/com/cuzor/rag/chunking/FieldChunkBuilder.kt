package com.cuzor.rag.chunking

import com.cuzor.rag.models.Ticket

object FieldChunkBuilder {

    fun build(ticket: Ticket): String {

        return """
Title:
${ticket.title}

Description:
${ticket.description}

Conversation:
${ticket.conversation}

Resolution:
${ticket.resolution}
""".trimIndent()

    }

}