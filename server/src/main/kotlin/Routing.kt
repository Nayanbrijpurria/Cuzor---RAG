package com.cuzor.rag

import com.cuzor.rag.routes.healthRoutes
import com.cuzor.rag.routes.ticketRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {

        // API routes
        healthRoutes()
        ticketRoutes()

        // Frontend
        staticResources(
            remotePath = "/",
            basePackage = "static"
        )
    }
}