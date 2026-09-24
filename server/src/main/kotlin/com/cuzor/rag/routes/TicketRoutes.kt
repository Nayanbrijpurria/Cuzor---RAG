package com.cuzor.rag.routes

import com.cuzor.rag.models.SearchRequest
import com.cuzor.rag.retrieval.RetrievalService
import com.cuzor.rag.repository.TicketRepository
import com.cuzor.rag.services.TicketQueryService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.ticketRoutes() {

    val ticketService =
        TicketQueryService()

    val retrievalService =
        RetrievalService()


    route("/tickets") {

        // ==========================================
        // GET TICKETS
        // ==========================================

        get {
            call.respond(
                ticketService.getTopFiveTickets()
            )
        }

        // ==========================================
        // VECTOR SEARCH
        // ==========================================

        post("/search") {

            val request =
                call.receive<SearchRequest>()

            println("===================================")
            println("Search Request Received")
            println("Query : ${request.query}")
            println("Limit : ${request.limit}")
            println("===================================")

            val results =
                retrievalService.retrieve(

                    query =
                        request.query,

                    limit =
                        request.limit
                )

            call.respond(
                results
            )
        }

        // ==========================================
// EMBEDDING HEALTH
// ==========================================

        get("/embedding/health") {

            println("===================================")
            println("Embedding Health Request")
            println("===================================")

            val health =
                TicketRepository.getEmbeddingHealth()

            call.respond(
                health
            )
        }

        // ==========================================
// FAILED EMBEDDINGS
// ==========================================

        get("/embedding/failed") {

            println("===================================")
            println("Failed Embeddings Request")
            println("===================================")

            val failedTickets =
                TicketRepository.findFailedEmbeddingTickets()

            call.respond(
                failedTickets
            )
        }

        // ==========================================
// RETRY FAILED EMBEDDING
// ==========================================

        post("/{ticketId}/embedding/retry") {

            val ticketId =
                call.parameters["ticketId"]

            if (ticketId == null) {

                call.respondText(
                    "Ticket ID is required",
                    status =
                        io.ktor.http.HttpStatusCode.BadRequest
                )

                return@post
            }

            println("===================================")
            println("Retry Embedding Request")
            println("Ticket ID : $ticketId")
            println("===================================")

            val ticket =
                TicketRepository.findById(
                    ticketId
                )

            if (ticket == null) {

                call.respondText(
                    "Ticket not found",
                    status =
                        io.ktor.http.HttpStatusCode.NotFound
                )

                return@post
            }

            TicketRepository.markEmbeddingPending(
                ticketId
            )

            call.respondText(
                "Embedding retry queued for ticket $ticketId"
            )
        }
        // ==========================================
        // GET COMPLETE TICKET
        // ==========================================

        get("/{ticketId}") {

            val ticketId =
                call.parameters["ticketId"]


            if (ticketId == null) {

                call.respondText(
                    "Ticket ID is required",
                    status =
                        io.ktor.http.HttpStatusCode.BadRequest
                )

                return@get
            }


            println("===================================")
            println("Ticket Detail Request")
            println("Ticket ID : $ticketId")
            println("===================================")


            val ticket =
                TicketRepository.findById(
                    ticketId
                )


            if (ticket == null) {

                call.respondText(
                    "Ticket not found",
                    status =
                        io.ktor.http.HttpStatusCode.NotFound
                )


                return@get
            }
            call.respond(
                ticket
            )
        }
    }
}