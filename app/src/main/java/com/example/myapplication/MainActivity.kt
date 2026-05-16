package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.Booking
import com.example.myapplication.data.MaintenanceItem
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "calendar",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("calendar") { CalendarScreen() }
            composable("booking") { BookingScreen() }
            composable("maintenance") { MaintenanceScreen() }
            composable("events") { EventBoardScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        NavigationBarItem(
            selected = currentRoute == "calendar",
            onClick = { navController.navigate("calendar") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
            label = { Text("Calendar") }
        )
        NavigationBarItem(
            selected = currentRoute == "booking",
            onClick = { navController.navigate("booking") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Book") },
            label = { Text("Book") }
        )
        NavigationBarItem(
            selected = currentRoute == "maintenance",
            onClick = { navController.navigate("maintenance") },
            icon = { Icon(Icons.Default.Build, contentDescription = "Maintenance") },
            label = { Text("Maintenance") }
        )
        NavigationBarItem(
            selected = currentRoute == "events",
            onClick = { navController.navigate("events") },
            icon = { Icon(Icons.Default.Info, contentDescription = "Events") },
            label = { Text("Events") }
        )
    }
}

@Composable
fun CalendarScreen(viewModel: MainViewModel = viewModel()) {
    val bookings by viewModel.bookings.collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Grama-Angana Calendar", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("View 'Booked' vs 'Free' slots", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bookings scheduled yet.")
            }
        } else {
            LazyColumn {
                items(bookings) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(if (booking.status == "Booked") Color.Red else Color.Green, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(booking.date, fontWeight = FontWeight.Bold)
                                Text("${booking.timeSlot} - ${booking.eventName}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(booking.status, color = if (booking.status == "Booked") Color.Red else Color.Green)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingScreen(viewModel: MainViewModel = viewModel()) {
    var date by remember { mutableStateOf("2025-05-15") }
    var timeSlot by remember { mutableStateOf("Morning (9AM-12PM)") }
    var eventName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Request Booking", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Form to ask the Panchayat for a slot", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = timeSlot, onValueChange = { timeSlot = it }, label = { Text("Time Slot") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = eventName, onValueChange = { eventName = it }, label = { Text("Event Name / Purpose") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (eventName.isBlank()) {
                    message = "Please enter an event name"
                    isError = true
                    return@Button
                }
                viewModel.requestBooking(date, timeSlot, eventName) { success ->
                    if (success) {
                        message = "Booking requested successfully!"
                        isError = false
                        eventName = ""
                    } else {
                        message = "Double Booking Prevented! Slot already taken."
                        isError = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Request")
        }
        
        if (message.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
            ) {
                Text(message, color = if (isError) Color.Red else Color(0xFF2E7D32), modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun MaintenanceScreen(viewModel: MainViewModel = viewModel()) {
    val items by viewModel.maintenanceItems.collectAsState(initial = emptyList())
    var newItemName by remember { mutableStateOf("") }
    var newItemTarget by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Maintenance Jar", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Items needed and pledge support", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add Maintenance Need", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(value = newItemName, onValueChange = { newItemName = it }, label = { Text("Item (e.g. Fan)") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = newItemTarget, onValueChange = { newItemTarget = it }, label = { Text("Cost ₹") }, modifier = Modifier.weight(0.5f))
                }
                Button(onClick = {
                    val target = newItemTarget.toDoubleOrNull() ?: 0.0
                    if (newItemName.isNotBlank() && target > 0) {
                        viewModel.addMaintenanceItem(newItemName, target)
                        newItemName = ""
                        newItemTarget = ""
                    }
                }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Add to Jar")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Active Jars", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(items) { item ->
                MaintenanceItemRow(item, onPledge = { viewModel.pledgeMaintenance(item, 50.0) })
            }
        }
    }
}

@Composable
fun MaintenanceItemRow(item: MaintenanceItem, onPledge: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("₹${item.currentAmount.toInt()} / ₹${item.targetAmount.toInt()}", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            val progress = if (item.targetAmount > 0) (item.currentAmount / item.targetAmount).toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onPledge, 
                enabled = item.currentAmount < item.targetAmount,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (item.currentAmount >= item.targetAmount) "Funded!" else "Pledge ₹50")
            }
        }
    }
}

@Composable
fun EventBoardScreen(viewModel: MainViewModel = viewModel()) {
    val bookings by viewModel.bookings.collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Event Board", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("See what's happening in the community hall", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(bookings) { booking ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(booking.eventName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${booking.date} | ${booking.timeSlot}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
