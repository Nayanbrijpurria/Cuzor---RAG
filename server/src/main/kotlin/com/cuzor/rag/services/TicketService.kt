package com.cuzor.rag.services

import com.cuzor.rag.models.Ticket
import com.cuzor.rag.repository.TicketRepository


class TicketService {

    fun saveTicket(ticket: Ticket) {

        TicketRepository.save(ticket)

    }

    fun saveAllTickets(tickets: List<Ticket>) {

        tickets.forEach {

            TicketRepository.save(it)

        }

    }

    fun totalTickets(): Long {

        return TicketRepository.count()

    }

}