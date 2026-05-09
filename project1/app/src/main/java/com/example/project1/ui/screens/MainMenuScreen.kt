package com.example.project1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    // 1. Ajustamos los nombres de los parámetros para que coincidan con MainNavigation
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToNewSale: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sistema de Ventas",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Botón Gestión de Clientes
        Button(
            onClick = onNavigateToCustomers,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Gestionar Clientes", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(20.dp))

        // Botón Gestión de Productos (NUEVO)
        Button(
            onClick = onNavigateToProducts,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)) // Color púrpura
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Gestionar Productos", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(20.dp))

        // Botón Cobros y Deudas
        Button(
            onClick = onNavigateToPayments,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Color verde
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Cobros y Deudas", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(20.dp))

        // Nueva venta
        Button(
            onClick = onNavigateToNewSale,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)) // Color verde
        ) {
            Icon(Icons.Default.Done, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Punto de venta", style = MaterialTheme.typography.titleMedium)
        }
    }
}