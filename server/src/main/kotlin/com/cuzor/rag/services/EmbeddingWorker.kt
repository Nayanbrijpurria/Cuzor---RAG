package com.cuzor.rag.services

import com.cuzor.rag.config.AppConfig
import com.cuzor.rag.models.EmbeddingStatus
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes


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

            /*
             * Run the first cycle immediately.
             *
             * We don't have to wait for the configured
             * interval after application startup.
             */

            processPendingTickets()


            /*
             * Continue running while the worker
             * coroutine is active.
             */

            while (isActive) {

                delay(
                    config.embeddingWorkerIntervalMinutes
                        .minutes
                )

                processPendingTickets()
            }
        }
    }


    /**
     * Find tickets with:
     *
     * embedding_verified = 0
     *
     * and generate their embeddings.
     *
     * Tickets with status 1 or 2 are ignored.
     */
    private suspend fun processPendingTickets() {

        val startTime =
            System.currentTimeMillis()


        println()
        println("===================================")
        println("Embedding Worker Cycle Started")
        println("===================================")


        try {

            // ==========================================
            // FIND PENDING TICKETS
            // ==========================================

            val pendingTickets =
                TicketRepository.findPendingEmbeddingTickets(
                    config.embeddingWorkerBatchSize
                )


            if (pendingTickets.isEmpty()) {

                println(
                    "No pending tickets found."
                )

                println(
                    "Embedding Worker Cycle Finished"
                )

                println("===================================")

                return
            }


            println(
                "Pending Tickets Found : " +
                        pendingTickets.size
            )


            var successful = 0
            var failed = 0
            var movedToFailed = 0


            // ==========================================
            // PROCESS EACH TICKET
            // ==========================================

            for (ticket in pendingTickets) {

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
                    //
                    // IMPORTANT:
                    //
                    // We only mark the ticket as EMBEDDED
                    // after the vector has successfully
                    // been saved to PostgreSQL.
                    //

                    TicketRepository.markEmbeddingVerified(
                        ticket.ticketId
                    )


                    val ticketTime =
                        System.currentTimeMillis() -
                                ticketStartTime


                    successful++


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
                }

                catch (exception: Exception) {

                    failed++


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

                        movedToFailed++


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
                                    "in future cycles."
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
                                    "in the next worker cycle."
                        )
                    }
                }
            }


            val totalTime =
                System.currentTimeMillis() -
                        startTime


            // ==========================================
            // CYCLE SUMMARY
            // ==========================================

            println()
            println("===================================")
            println("Embedding Worker Cycle Finished")
            println("===================================")

            println(
                "Processed : " +
                        pendingTickets.size
            )

            println(
                "Successful : " +
                        successful
            )

            println(
                "Failed Attempts : " +
                        failed
            )

            println(
                "Moved To FAILED : " +
                        movedToFailed
            )

            println(
                "Total Time : " +
                        totalTime +
                        " ms"
            )

            println("===================================")
        }

        catch (exception: Exception) {

            /*
             * This catches unexpected worker-level
             * errors.
             *
             * Individual ticket errors are already
             * handled above.
             */

            println(
                "❌ Embedding Worker Error"
            )

            println(
                "Reason : " +
                        exception.message
            )
        }
    }


    /**
     * Stop the worker.
     */
    fun stop() {

        workerScope.cancel()
    }
}