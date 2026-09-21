package com.cuzor.rag

import com.cuzor.rag.config.AppConfig
import com.cuzor.rag.configureRouting
import com.cuzor.rag.configureSerialization
import com.cuzor.rag.database.DatabaseFactory
import com.cuzor.rag.services.EmbeddingWorker
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    // ==========================================
    // APPLICATION CONFIGURATION
    // ==========================================

    val config = AppConfig(this)


    // ==========================================
    // KTOR CONFIGURATION
    // ==========================================

    configureSerialization()

    configureRouting()


    // ==========================================
    // DATABASE
    // ==========================================

    DatabaseFactory.init(config)


    // ==========================================
    // EMBEDDING WORKER
    // ==========================================

    val embeddingWorker =
        EmbeddingWorker(config)

    embeddingWorker.start()


    // ==========================================
    // APPLICATION STARTED
    // ==========================================

    println("===================================")
    println("Cuzor RAG Application Started")
    println("Embedding Worker Started")
    println("===================================")
}