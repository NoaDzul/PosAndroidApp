package com.example.project1.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.core.content.FileProvider
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Sale
import com.example.project1.data.local.enums.PaymentMethod
import com.example.project1.ui.report.exportToExcel
import com.example.project1.ui.util.formatDate
import com.example.project1.ui.viewmodel.POSViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCollectionScreen(viewModel: POSViewModel, onNavigateBack: () -> Unit) {
    val pendingSales by viewModel.pendingSales.collectAsState(initial = emptyList())
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val allPayments by viewModel.allPayments.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // <--- ESTA ES LA QUE TE FALTA
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showAbonoDialog by remember { mutableStateOf(false) }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cobranza y Deudas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (pendingSales.isNotEmpty()) {
                            // Dentro del onClick del botón de exportar:
                            val generado =
                                exportToExcel(context, pendingSales, customers, allPayments)

                            if (generado != null) {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Excel guardado en Descargas",
                                        actionLabel = "ABRIR",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        openExcelFile(
                                            context,
                                            generado
                                        ) // Usamos el archivo que devolvió la función
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "No hay datos para exportar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Exportar",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar cliente...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (filteredSales.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No hay deudas pendientes", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSales) { sale ->
                        val customerName =
                            customers.find { it.id == sale.customerId }?.name ?: "Desconocido"
                        val payments by viewModel.getPaymentsBySale(sale.id)
                            .collectAsState(initial = emptyList())

                        DebtCard(
                            sale = sale,
                            customerName = customerName,
                            lastPayment = payments.firstOrNull(),
                            onDetailClick = { selectedSale = sale; showDetailDialog = true },
                            onAbonoClick = { selectedSale = sale; showAbonoDialog = true }
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO DE DETALLES (ESTADO DE CUENTA COMPLETO) ---
        if (showDetailDialog && selectedSale != null) {
            val itemsWithProduct by viewModel.getItemsWithProductBySale(selectedSale!!.id)
                .collectAsState(initial = emptyList())
            val payments by viewModel.getPaymentsBySale(selectedSale!!.id)
                .collectAsState(initial = emptyList())
            val items by viewModel.getItemsBySale(selectedSale!!.id)
                .collectAsState(initial = emptyList())
            val totalAbonado = payments.sumOf { it.amount }

            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = {
                    Column {
                        Text("Historial de Cuenta", fontWeight = FontWeight.Bold)
                        Text(
                            text = customers.find { it.id == selectedSale!!.customerId }?.name
                                ?: "",
                            style = MaterialTheme.typography.bodySmall, color = Color.Gray
                        )
                    }
                },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "Productos Acumulados:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp
                        )
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))

                        itemsWithProduct.forEach { item ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${item.product.name} / $${
                                        String.format(
                                            "%.2f",
                                            item.saleItem.priceAtSale
                                        )
                                    } / x${item.saleItem.quantity}", fontSize = 13.sp
                                )
                                Text(
                                    "$${
                                        String.format(
                                            "%.2f",
                                            item.saleItem.priceAtSale * item.saleItem.quantity
                                        )
                                    }", fontSize = 13.sp
                                )
                            }
                        }

                        // DEUDA INICIAL
                        Surface(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(Modifier.padding(8.dp), Arrangement.SpaceBetween) {
                                Text(
                                    "DEUDA INICIAL:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "$${String.format("%.2f", selectedSale!!.saleTotal)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Historial de Abonos:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp
                        )
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))

                        payments.forEach { p ->
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(formatDate(p.date), fontSize = 12.sp)
                                Text(
                                    "- $${String.format("%.2f", p.amount)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // SUMA DE ABONOS
                        HorizontalDivider(Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("TOTAL PAGADO:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                "$${String.format("%.2f", totalAbonado)}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // SALDO RESTANTE (POR PAGAR)
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Text(
                                    "DEUDA PENDIENTE:",
                                    fontWeight = FontWeight.Black,
                                    color = Color.Red
                                )
                                Text(
                                    text = "$${
                                        String.format(
                                            "%.2f",
                                            selectedSale!!.remainingBalance
                                        )
                                    }",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDetailDialog = false
                    }) { Text("Cerrar") }
                }
            )
        }

        // --- DIÁLOGO DE ABONO ---
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
                        Text(
                            "Saldo Actual: $${selectedSale!!.remainingBalance}",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = abonoAmount,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() || c == '.' }) abonoAmount = it
                            },
                            label = { Text("Monto a abonar") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("$ ") }
                        )
                        OutlinedTextField(
                            value = payWith,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() || c == '.' }) payWith = it
                            },
                            label = { Text("Paga con") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("$ ") }
                        )
                        if (payWithValue > abonoValue && abonoValue > 0) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "CAMBIO:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        "$${String.format("%.2f", cambio)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (abonoValue > 0) {
                                viewModel.addPayment(
                                    selectedSale!!.id,
                                    abonoValue,
                                    PaymentMethod.CASH
                                )
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
fun DebtCard(
    sale: Sale,
    customerName: String,
    lastPayment: Payment?,
    onDetailClick: () -> Unit,
    onAbonoClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "Inició: ${formatDate(sale.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "SALDO ACTUAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${String.format("%.2f", sale.remainingBalance)}",
                        color = Color.Red,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(Modifier.height(12.dp))

            if (lastPayment != null) {
                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Último abono:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                formatDate(lastPayment.date),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "+ $${String.format("%.2f", lastPayment.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            } else {
                Text(
                    "Sin abonos registrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDetailClick,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Detalles", color = Color(0xFF2E7D32))
                }
                Button(
                    onClick = onAbonoClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Abonar")
                }
            }
        }
    }
}

private fun openExcelFile(context: android.content.Context, file: File) {
    try {
        // Creamos una URI segura usando el FileProvider que configuraste en el Manifest
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            // Seteamos el tipo de dato específico de Excel (MIME type)
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Abrimos el selector de aplicaciones
        context.startActivity(Intent.createChooser(intent, "Abrir reporte con:"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "No se encontró una aplicación para abrir Excel",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}