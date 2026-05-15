package com.example.project1.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Punto de Venta & Reservas",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onNavigateToCustomers,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Gestión de Clientes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToProducts,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Gestión de Productos")
        }
    }
}