package com.cuzor

import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.rpc.krpc.ktor.client.installKrpc

val httpClient = HttpClient {

    installKrpc()

    install(HttpTimeout) {
        requestTimeoutMillis = 120_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 120_000
    }
}