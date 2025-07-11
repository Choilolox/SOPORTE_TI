package com.empresa.soporte.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.empresa.soporte.model.Ticket

@Dao
interface TicketDao {
    @Query("SELECT * FROM ticket ORDER BY id DESC")
    fun getAll(): LiveData<List<Ticket>>

    @Insert
    suspend fun insert(ticket: Ticket)

    @Update
    suspend fun update(ticket: Ticket)

    @Delete
    suspend fun delete(ticket: Ticket)
}
