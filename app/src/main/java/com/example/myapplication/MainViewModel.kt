package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Booking
import com.example.myapplication.data.MaintenanceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val maintenanceItems: Flow<List<MaintenanceItem>> = dao.getAllMaintenanceItems()
    val bookings: Flow<List<Booking>> = dao.getAllBookings()

    fun addMaintenanceItem(name: String, target: Double) {
        viewModelScope.launch {
            dao.insertMaintenanceItem(MaintenanceItem(name = name, targetAmount = target, currentAmount = 0.0))
        }
    }

    fun pledgeMaintenance(item: MaintenanceItem, amount: Double) {
        viewModelScope.launch {
            dao.updateMaintenanceItem(item.copy(currentAmount = item.currentAmount + amount))
        }
    }

    suspend fun isSlotAvailable(date: String, timeSlot: String): Boolean {
        val bookingsOnDate = dao.getBookingsByDate(date)
        return bookingsOnDate.none { it.timeSlot == timeSlot }
    }

    fun requestBooking(date: String, timeSlot: String, eventName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (isSlotAvailable(date, timeSlot)) {
                dao.insertBooking(Booking(date = date, timeSlot = timeSlot, eventName = eventName, status = "Booked"))
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }
}
