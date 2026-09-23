

package com.cuzor.rag.config

import io.ktor.server.application.*

class AppConfig(application: Application) {

    // ==========================================
    // DATABASE CONFIGURATION
    // ==========================================

    val host =
        application.environment.config
            .property("database.host")
            .getString()

    val port =
        application.environment.config
            .property("database.port")
            .getString()
            .toInt()

    val database =
        application.environment.config
            .property("database.name")
            .getString()

    val username =
        application.environment.config
            .property("database.user")
            .getString()

    val password =
        application.environment.config
            .property("database.password")
            .getString()


    // ==========================================
    // EMBEDDING WORKER CONFIGURATION
    // ==========================================

    val embeddingWorkerIntervalMinutes =
        application.environment.config
            .property("embedding.worker.intervalMinutes")
            .getString()
            .toLong()

    val embeddingWorkerBatchSize =
        application.environment.config
            .property("embedding.worker.batchSize")
            .getString()
            .toInt()
}