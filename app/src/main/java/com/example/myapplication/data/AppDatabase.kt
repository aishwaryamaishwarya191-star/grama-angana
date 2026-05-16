package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM maintenance_items")
    fun getAllMaintenanceItems(): Flow<List<MaintenanceItem>>

    @Insert
    suspend fun insertMaintenanceItem(item: MaintenanceItem)

    @Update
    suspend fun updateMaintenanceItem(item: MaintenanceItem)

    @Query("SELECT * FROM bookings")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert
    suspend fun insertBooking(booking: Booking)

    @Query("SELECT * FROM bookings WHERE date = :date")
    suspend fun getBookingsByDate(date: String): List<Booking>
}

@Database(entities = [MaintenanceItem::class, Booking::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grama_angana_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
