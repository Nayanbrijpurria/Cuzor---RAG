package com.cuzor.rag.services

object ProductExtractor {

    private val products = listOf(
        "Mini UPS",
        "Pulse GaN Charger"
    )

    fun extract(conversation: String): String? {

        val customerMessages = conversation
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .lines()
            .filter { line ->
                line.contains("CUSTOMER:", ignoreCase = true)
            }
            .map { line ->
                line.substringAfter("CUSTOMER:")
                    .trim()
            }

        for (message in customerMessages) {

            val normalized = normalize(message)

            for (product in products) {

                if (normalize(product) == normalized) {
                    return product
                }
            }
        }

        return null
    }

    private fun normalize(value: String): String {
        return value
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
    }
}