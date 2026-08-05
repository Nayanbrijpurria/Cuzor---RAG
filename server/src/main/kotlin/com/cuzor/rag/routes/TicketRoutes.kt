package com.cuzor.rag.routes

import com.cuzor.rag.services.TicketQueryService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.cuzor.rag.retrieval.RetrievalService
import com.cuzor.rag.models.SearchRequest
import io.ktor.server.request.*
import io.ktor.http.*

fun Route.ticketRoutes() {

    val ticketService = TicketQueryService()
    val retrievalService = RetrievalService()
    route("/tickets") {

        get {

            call.respond(

                ticketService.getTopFiveTickets()

            )

        }

    }

    post("/search") {

        val request =
            call.receive<SearchRequest>()

        println("===================================")
        println("Search Request Received")
        println("Query : ${request.query}")
        println("===================================")

        val results =

            retrievalService.retrieve(

                request.query

            )

        call.respond(results)


    }

}