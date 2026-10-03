package com.cuzor.rag.csv

import com.cuzor.rag.models.Ticket
import com.opencsv.CSVReader
import java.io.FileReader

object CsvReader {

    fun read(filePath: String): List<Ticket> {

        val tickets = mutableListOf<Ticket>()

        CSVReader(FileReader(filePath)).use { csvReader ->

            val rows = csvReader.readAll()

            rows.drop(1).forEachIndexed { index, row ->

                val actualRowNumber = index + 2

                try {

                    if (row.size < 12) {

                        println("===================================")
                        println("INVALID CSV RECORD")
                        println("CSV Record : $actualRowNumber")
                        println("Columns    : ${row.size}")
                        println("Ticket ID  : ${row.getOrNull(0)}")
                        println("Skipping because required fields are missing.")
                        println("===================================")

                        return@forEachIndexed
                    }

                    val ticketId = row[0]
                    val created = row[1]
                    val closedAt = row[2]
                    val state = row[3]
                    val category = row[4]
                    val urgency = row[5].toIntOrNull() ?: 0
                    val customerName = row[6]
                    val customerPhone = row[7]
                    val messageCount = row[8].toIntOrNull() ?: 0
                    val title = row[9]
                    val description = row[10]

                    var resolution = ""
                    var conversation = ""

                    if (row.size >= 13) {

                        // Normal CSV record
                        resolution = row[11]
                        conversation = row[12]

                        // If there are unexpected extra columns,
                        // preserve them by adding them to conversation.
                        if (row.size > 13) {
                            conversation += row
                                .drop(13)
                                .joinToString(",")
                        }

                    } else {

                        // 12-column malformed record.
                        // In these records resolution and conversation
                        // are merged into column 11.

                        val combinedField = row[11]

                        val conversationStartRegex = Regex(
                            """(?=\[[A-Z][a-z]{2} \d{1,2}, \d{4} \d{1,2}:\d{2} [ap]m\])"""
                        )

                        val match = conversationStartRegex.find(combinedField)

                        if (match != null) {

                            val splitPosition = match.range.first

                            resolution = combinedField
                                .substring(0, splitPosition)
                                .trim()
                                .trimEnd(',')

                            conversation = combinedField
                                .substring(splitPosition)
                                .trim()

                        } else {

                            // We cannot find a conversation timestamp.
                            // Preserve all information rather than deleting it.
                            resolution = combinedField
                            conversation = ""
                        }

                        println("===================================")
                        println("REPAIRED 12-COLUMN RECORD")
                        println("CSV Record : $actualRowNumber")
                        println("Ticket ID  : $ticketId")
                        println("Resolution length : ${resolution.length}")
                        println("Conversation length : ${conversation.length}")
                        println("===================================")
                    }

                    val ticket = Ticket(

                        ticketId = ticketId,

                        created = created,

                        closedAt = closedAt,

                        state = state,

                        category = category,

                        urgency = urgency,

                        customerName = customerName,

                        customerPhone = customerPhone,

                        messageCount = messageCount,

                        title = title,

                        description = description,

                        resolution = resolution,

                        conversation = conversation
                    )

                    tickets.add(ticket)

                } catch (e: Exception) {

                    println("===================================")
                    println("CSV RECORD PARSING ERROR")
                    println("CSV Record : $actualRowNumber")
                    println("Ticket ID  : ${row.getOrNull(0)}")
                    println("Columns    : ${row.size}")
                    println("Error      : ${e.message}")
                    println("===================================")

                    // Continue with the next CSV record.
                }
            }
        }

        println("===================================")
        println("CSV PARSING FINISHED")
        println("Tickets Successfully Parsed : ${tickets.size}")
        println("===================================")

        return tickets
    }
}