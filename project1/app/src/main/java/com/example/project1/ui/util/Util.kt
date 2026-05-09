package com.example.project1.ui.util

import java.text.SimpleDateFormat
import java.util.*

// Esta función convierte el número largo (timestamp) en texto que podemos leer
fun formatDate(timestamp: Long): String {
    // "dd/MM/yyyy" -> Día/Mes/Año
    // "HH:mm" -> Horas:Minutos
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = Date(timestamp)
    return sdf.format(date)
}