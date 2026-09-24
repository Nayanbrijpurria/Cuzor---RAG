package com.cuzor.rag.services

import com.cuzor.rag.config.AppConfig
import com.cuzor.rag.models.EmbeddingStatus
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.cuzor.rag.models.Ticket

//our old method was doing two jobs:
//
//Querying pending tickets.
//Processing every ticket.
//
//The Flow now handles:
//1. Querying pending tickets.
//2. Processing every ticket.
//3. Handling the interval between queries.
//--------------------------------------------------------------------------
// This worker is responsible for running the background embedding pipeline.
//
// The worker uses Kotlin Flow to continuously receive pending tickets.
//
// The actual RAG embedding behavior is unchanged:
//
// PENDING
//    ↓
// generate embedding
//    ↓
// save vector
//    ↓
// EMBEDDED
//
// If embedding fails:
//
// first failure  → PENDING
// second failure → FAILED
class EmbeddingWorker(
    private val config: AppConfig
) {

    private val ticketEmbeddingService =
        TicketEmbeddingService()

    private val workerScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    // Maximum number of times a ticket will be attempted.
    private val maxEmbeddingAttempts = 2


    /**
     * Start the background embedding worker.
     */
    fun start() {

        println("===================================")
        println("Starting Embedding Worker")
        println("Using Kotlin Flow")
        println(
            "Interval : " +
                    config.embeddingWorkerIntervalMinutes +
                    " minutes"
        )

        println(
            "Batch Size : " +
                    config.embeddingWorkerBatchSize
        )

        println(
            "Max Attempts : " +
                    maxEmbeddingAttempts
        )

        println("===================================")


        workerScope.launch {

            TicketRepository
                .pendingEmbeddingFlow(
                    batchSize =
                        config.embeddingWorkerBatchSize,

                    intervalMinutes =
                        config.embeddingWorkerIntervalMinutes.toLong()
                )
                .collect { ticket ->

                    processTicket(ticket)
                }
        }
    }


    /**
     * Process one ticket received from the Flow.
     *
     * The embedding logic here is intentionally
     * the same as the previous worker.
     */
    private suspend fun processTicket(
        ticket: Ticket
    ) {

        println()
        println("-----------------------------------")

        println(
            "Processing Ticket : " +
                    ticket.ticketId
        )

        println(
            "Current Status : " +
                    EmbeddingStatus.fromCode(
                        ticket.embeddingVerified
                    )
        )

        println(
            "Previous Attempts : " +
                    ticket.embeddingAttempts
        )


        try {

            val ticketStartTime =
                System.currentTimeMillis()


            // ==================================
            // GENERATE EMBEDDING
            // ==================================

            val embedding =
                ticketEmbeddingService.generate(
                    ticket
                )


            // ==================================
            // SAVE EMBEDDING
            // ==================================

            EmbeddingRepository.save(
                embedding
            )


            // ==================================
            // MARK AS EMBEDDED
            // ==================================

            // Important:
            //
            // The ticket is marked EMBEDDED
            // only after the vector has successfully
            // been saved to PostgreSQL.

            TicketRepository.markEmbeddingVerified(
                ticket.ticketId
            )


            val ticketTime =
                System.currentTimeMillis() -
                        ticketStartTime


            println(
                "Embedding completed : " +
                        ticket.ticketId
            )

            println(
                "Time : ${ticketTime} ms"
            )

            println(
                "Status : " +
                        EmbeddingStatus.EMBEDDED +
                        " (1)"
            )

        } catch (exception: Exception) {

            println()

            println(
                "❌ Embedding failed : " +
                        ticket.ticketId
            )

            println(
                "Reason : " +
                        exception.message
            )


            // ==================================
            // INCREMENT ATTEMPT COUNT
            // ==================================

            val attempts =
                TicketRepository
                    .incrementEmbeddingAttempts(
                        ticket.ticketId
                    )


            println(
                "Embedding Attempts : " +
                        attempts
            )


            // ==================================
            // DECIDE WHETHER TO RETRY
            // ==================================

            if (
                attempts >=
                maxEmbeddingAttempts
            ) {

                // --------------------------------
                // SECOND FAILURE
                // --------------------------------

                TicketRepository
                    .markEmbeddingFailed(
                        ticket.ticketId
                    )


                println(
                    "Status : " +
                            EmbeddingStatus.FAILED +
                            " (2)"
                )

                println(
                    "Maximum attempts reached."
                )

                println(
                    "Worker will skip this ticket " +
                            "in future Flow cycles."
                )

            } else {

                // --------------------------------
                // FIRST FAILURE
                // --------------------------------

                println(
                    "Status : " +
                            EmbeddingStatus.PENDING +
                            " (0)"
                )

                println(
                    "Ticket will be retried " +
                            "in the next Flow cycle."
                )
            }
        }
    }


    /**
     * Stop the worker.
     *
     * Cancelling the worker scope automatically
     * cancels Flow collection as well.
     */
    fun stop() {

        println(
            "Stopping Embedding Worker..."
        )

        workerScope.cancel()

        println(
            "Embedding Worker stopped."
        )
    }
}