package com.example.project1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.project1.data.local.entities.Customer
import com.example.project1.ui.viewmodel.POSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCrudScreen(viewModel: POSViewModel, onNavigateBack: () -> Unit) {
    val customers by viewModel.customers.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showBulkDialog by remember { mutableStateOf(false) } // Nuevo: Diálogo masivo
    var showDeleteConfirm by remember { mutableStateOf(false) } // Nuevo: Confirmación
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Directorio de Clientes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón para creación masiva
                    IconButton(onClick = { showBulkDialog = true }) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = "Masivo")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedCustomer = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // BUSCADOR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o teléfono...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No hay resultados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCustomers) { customer ->
                        CustomerItem(
                            customer = customer,
                            onEdit = {
                                selectedCustomer = customer
                                showDialog = true
                            },
                            onDelete = {
                                customerToDelete = customer
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            }
        }

        // DIÁLOGO INDIVIDUAL
        if (showDialog) {
            CustomerDialog(
                customer = selectedCustomer,
                onDismiss = { showDialog = false },
                onConfirm = { name, phone ->
                    if (selectedCustomer == null) {
                        viewModel.saveCustomer(name, phone)
                    } else {
                        viewModel.updateCustomer(
                            selectedCustomer!!.copy(
                                name = name,
                                phone = phone
                            )
                        )
                    }
                    showDialog = false
                }
            )
        }

        // DIÁLOGO MASIVO
        if (showBulkDialog) {
            BulkCustomerDialog(
                onDismiss = { showBulkDialog = false },
                onConfirm = { namesString ->
                    // Separar por comas, limpiar espacios y filtrar vacíos
                    val names = namesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    names.forEach { name ->
                        viewModel.saveCustomer(name, "") // Teléfono vacío por defecto
                    }
                    showBulkDialog = false
                }
            )
        }

        // DIÁLOGO DE CONFIRMACIÓN PARA ELIMINAR
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Eliminar Cliente") },
                text = { Text("¿Estás seguro de eliminar a ${customerToDelete?.name}? Esto podría afectar sus reservas activas.") },
                confirmButton = {
                    Button(
                        onClick = {
                            customerToDelete?.let { viewModel.deleteCustomer(it) }
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun BulkCustomerDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Creación Masiva") },
        text = {
            Column {
                Text(
                    "Escribe los nombres separados por comas:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Ej: Juan Perez, Maria Lopez, Pedro...") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(input) }, enabled = input.isNotBlank()) {
                Text("Crear Todos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun CustomerItem(
    customer: Customer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "📞 ${customer.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF1976D2)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (customer == null) "Nuevo Cliente" else "Editar Cliente")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono / Celular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, phone) },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}