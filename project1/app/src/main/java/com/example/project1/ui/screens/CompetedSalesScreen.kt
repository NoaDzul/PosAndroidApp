package com.example.project1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project1.data.local.entities.Sale
import com.example.project1.ui.util.formatDate
import com.example.project1.ui.viewmodel.POSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedSalesScreen(viewModel: POSViewModel, onNavigateBack: () -> Unit) {
    // Observamos los flujos del ViewModel
    val completedSales by viewModel.completedSales.collectAsState(initial = emptyList())
    val customers by viewModel.customers.collectAsState(initial = emptyList())

    // Estados para búsqueda y diálogo
    var searchQuery by remember { mutableStateOf("") }
    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // Filtrado lógico por nombre de cliente
    val filteredSales = remember(searchQuery, completedSales, customers) {
        if (searchQuery.isEmpty()) {
            completedSales
        } else {
            completedSales.filter { sale ->
                val name = customers.find { it.id == sale.customerId }?.name ?: ""
                name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Ventas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- BUSCADOR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar cliente...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1B5E20),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // --- LISTADO ---
            if (filteredSales.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "No hay ventas liquidadas" else "Sin resultados",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredSales) { sale ->
                        val customerName = customers.find { it.id == sale.customerId }?.name ?: "Cliente Desconocido"

                        // Tarjeta de Venta Individual
                        Card(
                            onClick = {
                                selectedSale = sale
                                showDetailDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Finalizado el: ${formatDate(sale.date)}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PAGADO", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    Text(
                                        "$${String.format("%.2f", sale.saleTotal)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1B5E20),
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DIÁLOGO DE DETALLES ---
        if (showDetailDialog && selectedSale != null) {
            val sale = selectedSale!!
            val items by viewModel.getItemsBySale(sale.id).collectAsState(initial = emptyList())
            val payments by viewModel.getPaymentsBySale(sale.id).collectAsState(initial = emptyList())
            val products by viewModel.availableProducts.collectAsState(initial = emptyList())
            val customerName = customers.find { it.id == sale.customerId }?.name ?: "Desconocido"

            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = {
                    Column {
                        Text("Detalles de Venta", fontWeight = FontWeight.Bold)
                        Text(customerName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Text("Productos", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 14.sp)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))

                        items.forEach { item ->
                            val productName = products.find { it.id == item.productId }?.name ?: "Producto #${item.productId}"
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), Arrangement.SpaceBetween) {
                                Text("• $productName x${item.quantity}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("$${String.format("%.2f", item.priceAtSale * item.quantity)}", fontSize = 13.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Historial de Abonos", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 14.sp)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))

                        payments.forEach { payment ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), Arrangement.SpaceBetween) {
                                Column {
                                    Text(formatDate(payment.date), fontSize = 12.sp)
                                    Text(payment.paymentMethod.name, fontSize = 10.sp, color = Color.Gray)
                                }
                                Text("$${String.format("%.2f", payment.amount)}", fontSize = 13.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("TOTAL LIQUIDADO", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$${String.format("%.2f", sale.saleTotal)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1B5E20))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDetailDialog = false }) { Text("Cerrar") }
                }
            )
        }
    }
}