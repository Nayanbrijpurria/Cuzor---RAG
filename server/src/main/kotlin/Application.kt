package com.cuzor.rag

import com.cuzor.rag.config.AppConfig
import com.cuzor.rag.configureRouting
import com.cuzor.rag.database.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import com.cuzor.rag.csv.CsvReader
import com.cuzor.rag.repository.TicketRepository
import com.cuzor.rag.services.TicketEmbeddingService
import kotlinx.coroutines.launch


//for CPU optimization

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cuzor.rag.configureSerialization
import kotlinx.coroutines.GlobalScope

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val config = AppConfig(this)
    val ticketEmbeddingService = TicketEmbeddingService()

    println(environment.config.toMap())

    configureSerialization()

    configureRouting()

    DatabaseFactory.init(config)

    println("===================================")
    println("Starting Ticket Import...")
    println("===================================")
    val tickets =
         CsvReader.read("C:\\Users\\ASUS\\Downloads\\live-tickets-june-2026-rag.csv")

    println("Tickets Found : ${tickets.size}")

     GlobalScope.launch {

        for (ticket in tickets) {

            TicketRepository.save(ticket)

            ticketEmbeddingService.save(ticket)

        }

    }

    println("===================================")
    println("Ticket Import Finished")
    println("===================================")


    val ticket = CsvReader.read(
        "C:\\Users\\ASUS\\Downloads\\live-tickets-june-2026-rag.csv"
    )

    println("Total Tickets = ${ticket.size}")

    ticket.take(3).forEach {
        println(it)
    }
}