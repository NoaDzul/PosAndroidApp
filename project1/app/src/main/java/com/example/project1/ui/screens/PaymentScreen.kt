package com.example.project1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Sale
import com.example.project1.data.local.enums.PaymentMethod
import com.example.project1.ui.report.exportToExcel
import com.example.project1.ui.util.formatDate
import com.example.project1.ui.viewmodel.POSViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCollectionScreen(viewModel: POSViewModel, onNavigateBack: () -> Unit) {
    val pendingSales by viewModel.pendingSales.collectAsState(initial = emptyList())
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val allPayments by viewModel.allPayments.collectAsState(initial = emptyList())
    val context = LocalContext.current

    // Estado para el texto del buscador
    var searchQuery by remember { mutableStateOf("") }

    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showAbonoDialog by remember { mutableStateOf(false) }

    // Lógica de filtrado: Filtra las ventas cuyo cliente coincida con la búsqueda
    val filteredSales = remember(searchQuery, pendingSales, customers) {
        if (searchQuery.isEmpty()) {
            pendingSales
        } else {
            pendingSales.filter { sale ->
                val name = customers.find { it.id == sale.customerId }?.name ?: ""
                name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cobranza y Deudas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    // --- BOTÓN DE EXPORTAR ---
                    IconButton(onClick = {
                        if (pendingSales.isNotEmpty()) {
                            Toast.makeText(context, "Generando Excel...", Toast.LENGTH_SHORT).show()
                            // Llamamos a la función de exportación
                            exportToExcel(
                                context = context,
                                sales = pendingSales,
                                customers = customers,
                                allPayments = allPayments
                            )
                        } else {
                            Toast.makeText(context, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Exportar Excel", tint = Color(0xFF2E7D32))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- BARRA DE BÚSQUEDA ---
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
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            if (filteredSales.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No hay deudas pendientes" else "No se encontraron resultados",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSales) { sale ->
                        val customerName = customers.find { it.id == sale.customerId }?.name ?: "Cliente desconocido"
                        val payments by viewModel.getPaymentsBySale(sale.id).collectAsState(initial = emptyList())
                        val lastPayment = payments.firstOrNull()

                        DebtCard(
                            sale = sale,
                            customerName = customerName,
                            lastPayment = lastPayment,
                            onDetailClick = {
                                selectedSale = sale
                                showDetailDialog = true
                            },
                            onAbonoClick = {
                                selectedSale = sale
                                showAbonoDialog = true
                            }
                        )
                    }
                }
            }
        }

        // --- DIÁLOGOS (Se mantienen igual) ---
        if (showDetailDialog && selectedSale != null) {
            val payments by viewModel.getPaymentsBySale(selectedSale!!.id).collectAsState(initial = emptyList())
            val items by viewModel.getItemsBySale(selectedSale!!.id).collectAsState(initial = emptyList())

            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = { Text("Historial de Cuenta", fontWeight = FontWeight.Bold) },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Venta realizada el: ${formatDate(selectedSale!!.date)}", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("Productos:", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                        items.forEach { item ->
                            Text("• Prod ID ${item.productId} | Cant: ${item.quantity} | $${item.priceAtSale}", fontSize = 13.sp)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        Text("Historial de Abonos:", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                        payments.forEach { p ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(formatDate(p.date), fontSize = 12.sp)
                                Text("$${p.amount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showDetailDialog = false }) { Text("Cerrar") } }
            )
        }

        if (showAbonoDialog && selectedSale != null) {
            var abonoAmount by remember { mutableStateOf("") }
            var payWith by remember { mutableStateOf("") }
            val abonoValue = abonoAmount.toDoubleOrNull() ?: 0.0
            val payWithValue = payWith.toDoubleOrNull() ?: 0.0
            val cambio = (payWithValue - abonoValue).coerceAtLeast(0.0)

            AlertDialog(
                onDismissRequest = { showAbonoDialog = false },
                title = { Text("Registrar Abono") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Deuda actual: $${selectedSale!!.remainingBalance}", color = Color.Red, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = abonoAmount,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) abonoAmount = it },
                            label = { Text("Monto del abono") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("$ ") }
                        )
                        OutlinedTextField(
                            value = payWith,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) payWith = it },
                            label = { Text("Pago con billete de") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("$ ") }
                        )
                        if (payWithValue > abonoValue && abonoValue > 0) {
                            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CAMBIO A ENTREGAR:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                    Text("$${String.format("%.2f", cambio)}", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (abonoValue > 0) {
                                viewModel.addPayment(selectedSale!!.id, abonoValue, PaymentMethod.CASH)
                                showAbonoDialog = false
                            }
                        },
                        enabled = abonoValue > 0 && abonoValue <= selectedSale!!.remainingBalance,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("Guardar Abono") }
                }
            )
        }
    }
}

@Composable
fun DebtCard(sale: Sale, customerName: String, lastPayment: Payment?, onDetailClick: () -> Unit, onAbonoClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Text(text = "Compra: ${formatDate(sale.date)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DEUDA ACTUAL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "$${String.format("%.2f", sale.remainingBalance)}", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(Modifier.height(12.dp))

            if (lastPayment != null) {
                Surface(color = Color(0xFFF1F8E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Último abono:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                            Text(formatDate(lastPayment.date), style = MaterialTheme.typography.bodySmall)
                        }
                        Text(text = "+ $${String.format("%.2f", lastPayment.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Text("Sin abonos registrados", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDetailClick, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, Color(0xFF2E7D32))) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Detalles", color = Color(0xFF2E7D32))
                }
                Button(onClick = onAbonoClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Abonar")
                }
            }
        }
    }
}
