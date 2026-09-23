package com.cuzor.rag.services

import com.cuzor.rag.embeddings.EmbeddingService
import com.cuzor.rag.models.EmbeddingVector
import com.cuzor.rag.models.Ticket
import com.cuzor.rag.repository.EmbeddingRepository

// This file is responsible for generating embeddings for tickets and saving them to the database.

class TicketEmbeddingService {

    private val embeddingService =
        EmbeddingService()


    private fun buildEmbeddingText(
        ticket: Ticket
    ): String {

        val text = buildString {

            // =========================================
            // CATEGORY
            // =========================================

            if (ticket.category.isNotBlank()) {

                append("Category: ")
                append(ticket.category.trim())
                append("\n\n")
            }


            // ==========================================
            // TITLE
            // ==========================================

            val cleanedTitle =

                cleanTitle(ticket.title)


            if (cleanedTitle.isNotBlank()) {

                append("Issue: ")
                append(cleanedTitle)
                append("\n\n")
            }


            // ==========================================
            // DESCRIPTION
            // ==========================================

            if (ticket.description.isNotBlank()) {

                append("Description:\n")
                append(
                    cleanGeneralText(
                        ticket.description
                    )
                )

                append("\n\n")
            }


            // ==========================================
            // RESOLUTION
            // ==========================================

            if (ticket.resolution.isNotBlank()) {

                append("Resolution:\n")
                append(
                    cleanGeneralText(
                        ticket.resolution
                    )
                )

                append("\n\n")
            }


            // ==========================================
            // CUSTOMER + AGENT CONVERSATION
            // ==========================================

            val usefulConversation =
                cleanConversation(
                    ticket.conversation
                )

            if (usefulConversation.isNotBlank()) {

                append("Support Conversation:\n")
                append(usefulConversation)
            }
        }

        return text
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }


    /**
     * Removes generic title noise.
     *
     * Example:
     *
     * "Live ticket for Ajay Singh"
     *
     * becomes:
     *
     * ""
     *
     * because the customer's name is not useful for semantic retrieval.
     */
    private fun cleanTitle(
        title: String
    ): String {

        var cleaned =
            title.trim()

        cleaned =
            cleaned.replace(
                Regex(
                    "(?i)^live\\s+ticket\\s+for\\s+"
                ),
                ""
            )

        return cleaned.trim()
    }


    /**
     * Cleans description and resolution fields.
     *
     * We intentionally do NOT aggressively modify
     * these fields because they may contain important
     * technical information.
     */
    private fun cleanGeneralText(
        text: String
    ): String {


        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
    /**
     * Extract only useful human conversation.
     *
     * We keep:
     *
     * CUSTOMER
     * AGENT
     *
     * We remove:
     *
     * BOT
     * timestamps
     * media placeholders
     * empty messages
     *
     * This prevents large amounts of chatbot
     * navigation text from dominating the embedding.
     */
    private fun cleanConversation(
        conversation: String
    ): String {

        if (conversation.isBlank()) {
            return ""
        }

        val cleanedLines =
            mutableListOf<String>()

        val lines =
            conversation
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .split("\n")


        for (line in lines) {

            var currentLine =
                line.trim()

            if (currentLine.isBlank()) {
                continue
            }


            // ==========================================
            // Remove timestamp
            //
            // [Jun 01, 2026 01:03 am]
            // ==========================================

            currentLine =
                currentLine.replace(
                    Regex(
                        "^\\[[^\\]]+\\]\\s*"
                    ),
                    ""
                )

            // ==========================================
            // Ignore media placeholders
            // ==========================================

            if (
                currentLine.equals(
                    "[media]",
                    ignoreCase = true
                )
            ) {
                continue
            }


            // ==========================================
            // Keep CUSTOMER messages
            // ==========================================

            if (
                currentLine.startsWith(
                    "CUSTOMER:",
                    ignoreCase = true
                )
            ) {

                val message =
                    currentLine.substringAfter(
                        ":"
                    ).trim()

                if (message.isNotBlank()) {

                    cleanedLines.add(
                        "CUSTOMER: $message"
                    )
                }

                continue
            }


            // ==========================================
            // Keep AGENT messages
            // ==========================================

            if (
                currentLine.startsWith(
                    "AGENT:",
                    ignoreCase = true
                )
            ) {

                val message =
                    currentLine.substringAfter(
                        ":"
                    ).trim()

                if (message.isNotBlank()) {

                    cleanedLines.add(
                        "AGENT: $message"
                    )
                }

                continue
            }

            /*
             * Everything else is ignored.
             *
             * This includes BOT messages and their
             * navigation/menu text.
             */
        }


        return cleanedLines.joinToString(
            separator = "\n"
        )
    }


    /**
     * Generate embedding for a ticket.
     */
    suspend fun generate(
        ticket: Ticket
    ): EmbeddingVector {

        val embeddingText =
            buildEmbeddingText(ticket)


        println(
            "==================================="
        )

        println(
            "Generating embedding for Ticket : " +
                    ticket.ticketId
        )

        println(
            "Embedding text length : " +
                    embeddingText.length
        )

        println(
            "Embedding text:"
        )

        println(
            embeddingText
        )

        println(
            "==================================="
        )


        val vector =
            embeddingService.createEmbedding(
                embeddingText
            )


        return EmbeddingVector(

            ticketId =
                ticket.ticketId,

            vector =
                vector
        )
    }


    /**
     * Generate and save embedding.
     */
    suspend fun save(
        ticket: Ticket
    ) {

        val embedding =
            generate(ticket)

        EmbeddingRepository.save(
            embedding
        )
    }
}