package com.cuzor.rag.database

import com.cuzor.rag.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.cuzor.rag.database.VectorTable
object DatabaseFactory {

    fun init(config: AppConfig) {

        val hikariConfig = HikariConfig().apply {

            jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"

            driverClassName = "org.postgresql.Driver"

            username = config.username

            password = config.password

            maximumPoolSize = 10

            isAutoCommit = false

            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        }

        try {
            val dataSource = HikariDataSource(hikariConfig)
            Database.connect(dataSource)
            transaction {

                SchemaUtils.create(

                    TicketTable,

                    VectorTable

                )

            }
            println("✅ PostgreSQL Connected Successfully")
        } catch (exception: Exception) {
            println("⚠️ PostgreSQL connection unavailable: ${exception.message}")
        }

    }

}