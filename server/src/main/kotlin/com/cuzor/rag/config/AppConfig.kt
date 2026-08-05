package com.cuzor.rag.config

import io.ktor.server.application.*

class AppConfig(application: Application) {

    val host =
        application.environment.config.property("database.host").getString()

    val port =
        application.environment.config.property("database.port").getString().toInt()

    val database =
        application.environment.config.property("database.name").getString()

    val username =
        application.environment.config.property("database.user").getString()

    val password =
        application.environment.config.property("database.password").getString()

}