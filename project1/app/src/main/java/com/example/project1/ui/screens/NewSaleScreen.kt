package com.example.project1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.project1.data.local.entities.Customer
import com.example.project1.data.local.enums.PaymentMethod
import com.example.project1.ui.viewmodel.POSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(viewModel: POSViewModel, onNavigateBack: () -> Unit) {
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val products by viewModel.availableProducts.collectAsState(initial = emptyList())

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var showCustomerSheet by remember { mutableStateOf(false) }
    var showProductSheet by remember { mutableStateOf(false) }

    // Estados para el Cobro
    var showPaymentDialog by remember { mutableStateOf(false) }
    var amountReceivedText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var expandedMethod by remember { mutableStateOf(false) }

    val totalSale = viewModel.cartItems.entries.sumOf { it.key.salePrice * it.value }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Venta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // SECCIÓN: SELECCIONAR CLIENTE
            Card(
                onClick = { showCustomerSheet = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCustomer == null)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = selectedCustomer?.name ?: "Seleccionar Cliente *",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // SECCIÓN: CARRITO
            Text("Productos en el pedido", style = MaterialTheme.typography.titleSmall)
            Box(modifier = Modifier.weight(1f)) {
                if (viewModel.cartItems.isEmpty()) {
                    Text("El carrito está vacío", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                } else {
                    LazyColumn {
                        items(viewModel.cartItems.toList()) { (product, quantity) ->
                            ListItem(
                                headlineContent = { Text(product.name, fontWeight = FontWeight.Bold) },
                                supportingContent = {
                                    Column {
                                        Surface(
                                            color = if (product.stock <= 5) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Stock: ${product.stock}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (product.stock <= 5) Color.Red else Color(0xFF2E7D32)
                                            )
                                        }
                                        Text("Subtotal: $${product.salePrice * quantity}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                trailingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.removeFromCart(product) }) {
                                            Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = null)
                                        }
                                        Text("$quantity", style = MaterialTheme.typography.titleMedium)
                                        IconButton(
                                            onClick = { viewModel.addToCart(product) },
                                            enabled = quantity < product.stock
                                        ) {
                                            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null)
                                        }
                                        IconButton(onClick = { viewModel.deleteFromCart(product) }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                        }
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // SECCIÓN: TOTAL Y ACCIONES
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("$${totalSale}", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showProductSheet = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Producto")
                        }
                        Button(
                            onClick = {
                                amountReceivedText = totalSale.toString() // Sugerir pago total
                                showPaymentDialog = true
                            },
                            enabled = selectedCustomer != null && viewModel.cartItems.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Cobrar")
                        }
                    }
                }
            }
        }


        // 1. DIÁLOGO DE COBRO (Calcula Cambio o Deuda)
        if (showPaymentDialog) {
            val received = amountReceivedText.toDoubleOrNull() ?: 0.0
            val difference = received - totalSale

            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                title = { Text("Finalizar Pago") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Total Venta: $${totalSale}", fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = amountReceivedText,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountReceivedText = it },
                            label = { Text("Monto Recibido") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        // Selector de Método de Pago
                        ExposedDropdownMenuBox(
                            expanded = expandedMethod,
                            onExpandedChange = { expandedMethod = !expandedMethod }
                        ) {
                            OutlinedTextField(
                                value = selectedMethod.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Método") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMethod) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedMethod, onDismissRequest = { expandedMethod = false }) {
                                PaymentMethod.entries.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method.name) },
                                        onClick = { selectedMethod = method; expandedMethod = false }
                                    )
                                }
                            }
                        }

                        // Resumen Dinámico
                        if (amountReceivedText.isNotEmpty()) {
                            val label = if (difference >= 0) "CAMBIO:" else "DEUDA RESTANTE:"
                            val color = if (difference >= 0) Color(0xFF2E7D32) else Color.Red
                            Text("$label $${String.format("%.2f", Math.abs(difference))}", color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Si paga de más, el abono registrado es solo el total de la venta
                            val finalAbono = if (received > totalSale) totalSale else received

                            selectedCustomer?.let { customer ->
                                viewModel.finalizeSale(customer, finalAbono, selectedMethod)
                            }
                            showPaymentDialog = false
                            onNavigateBack() // Regresa al menú principal
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (difference >= 0) "Completar Venta" else "Registrar con Deuda")
                    }
                }
            )
        }

        // 2. DIÁLOGO SELECCIÓN CLIENTE
        if (showCustomerSheet) {
            var q by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCustomerSheet = false },
                title = { Text("Seleccionar Cliente") },
                text = {
                    Column {
                        OutlinedTextField(value = q, onValueChange = { q = it }, placeholder = { Text("Buscar...") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.fillMaxHeight(0.5f)) {
                            items(customers.filter { it.name.contains(q, true) }) { c ->
                                TextButton(
                                    onClick = { selectedCustomer = c; showCustomerSheet = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(c.name, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
                                }
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showCustomerSheet = false }) { Text("Cerrar") } }
            )
        }

        // 3. DIÁLOGO SELECCIÓN PRODUCTOS
        if (showProductSheet) {
            AlertDialog(
                onDismissRequest = { showProductSheet = false },
                title = { Text("Agregar Productos") },
                text = {
                    LazyColumn(Modifier.fillMaxHeight(0.6f)) {
                        items(products) { p ->
                            ListItem(
                                headlineContent = { Text(p.name) },
                                supportingContent = { Text("Stock: ${p.stock} | $${p.salePrice}") },
                                trailingContent = {
                                    IconButton(
                                        onClick = { viewModel.addToCart(p) },
                                        enabled = (viewModel.cartItems[p] ?: 0) < p.stock
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                },
                confirmButton = { Button(onClick = { showProductSheet = false }) { Text("Listo") } }
            )
        }
    }
}