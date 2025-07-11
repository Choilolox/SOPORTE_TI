package com.empresa.soporte.data

import androidx.lifecycle.LiveData
import com.empresa.soporte.model.Ticket

class TicketRepository(private val dao: TicketDao) {
    val allTickets: LiveData<List<Ticket>> = dao.getAll()

    suspend fun insert(ticket: Ticket) = dao.insert(ticket)
    suspend fun update(ticket: Ticket) = dao.update(ticket)
    suspend fun delete(ticket: Ticket) = dao.delete(ticket)
}
