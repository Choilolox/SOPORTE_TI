package com.empresa.soporte.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.empresa.soporte.model.Ticket

@Database(entities = [Ticket::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
}
