
package com.cuzor.rag.routes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes() {

    get("/") {
        call.respondText("OK")
    }

    get("/health") {
        call.respondText("Server is healthy")
    }

}