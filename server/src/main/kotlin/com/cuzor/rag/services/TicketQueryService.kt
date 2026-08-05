package com.cuzor.rag.services

import com.cuzor.rag.models.Ticket
import com.cuzor.rag.repository.TicketRepository

class TicketQueryService {

    fun getTopFiveTickets(): List<Ticket> {

        return TicketRepository.findTop5()

    }

}