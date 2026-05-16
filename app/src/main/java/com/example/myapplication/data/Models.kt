package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_items")
data class MaintenanceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // Format: YYYY-MM-DD
    val timeSlot: String,
    val eventName: String,
    val status: String // "Free", "Booked", "Pending"
)
