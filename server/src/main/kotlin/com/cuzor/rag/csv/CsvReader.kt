package com.cuzor.rag.csv

import com.cuzor.rag.models.Ticket
import com.opencsv.CSVReader
import java.io.FileReader

object CsvReader {

    fun read(filePath: String): List<Ticket> {

        val tickets = mutableListOf<Ticket>()

        val csvReader = CSVReader(FileReader(filePath))

        val rows = csvReader.readAll()

        rows.drop(1).forEach { row ->

            val ticket = Ticket(

                ticketId = row[0],

                created = row[1],

                closedAt = row[2],

                state = row[3],

                category = row[4],

                urgency = row[5].toInt(),

                customerName = row[6],

                customerPhone = row[7],

                messageCount = row[8].toInt(),

                title = row[9],

                description = row[10],

                resolution = row[11],

                conversation = row[12]

            )

            tickets.add(ticket)

        }

        csvReader.close()

        return tickets

    }

}