package com.cuzor.rag.services

import com.cuzor.rag.config.AppConfig
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.cancel

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
        println("===================================")


        workerScope.launch {

            /*
             * Run the first cycle immediately.
             *
             * We don't have to wait 5 minutes after
             * application startup.
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
     * Find tickets with embedding_verified = 0
     * and generate their embeddings.
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
                    // MARK AS VERIFIED
                    // ==================================
                    //
                    // This happens ONLY after the
                    // embedding has been successfully
                    // saved.
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
                        "Status : VERIFIED (1)"
                    )
                }

                catch (exception: Exception) {

                    failed++


                    println(
                        "❌ Embedding failed : " +
                                ticket.ticketId
                    )

                    println(
                        "Reason : " +
                                exception.message
                    )

                    println(
                        "Status : PENDING (0)"
                    )

                    /*
                     * IMPORTANT:
                     *
                     * We intentionally DO NOT call
                     *
                     * markEmbeddingVerified()
                     *
                     * here.
                     *
                     * Therefore the ticket remains:
                     *
                     * embedding_verified = 0
                     *
                     * and will be retried during
                     * the next worker cycle.
                     */
                }
            }


            val totalTime =
                System.currentTimeMillis() -
                        startTime


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
                "Failed : " +
                        failed
            )

            println(
                "Total Time : " +
                        totalTime +
                        " ms"
            )

            println("===================================")
        }

        catch (exception: Exception) {

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