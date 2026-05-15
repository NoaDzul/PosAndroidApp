package com.example.project1.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.project1.data.local.entities.Product
import com.example.project1.ui.viewmodel.POSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCrudScreen(
    viewModel: POSViewModel,
    onNavigateBack: () -> Unit,
) {
    val products by viewModel.availableProducts.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Productos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedProduct = null
                showAddDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onEdit = {
                            selectedProduct = product
                            showAddDialog = true
                        },
                        onDelete = {
                            productToDelete = product
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // DIÁLOGO PARA AGREGAR / EDITAR
        if (showAddDialog) {
            AddProductDialog(
                product = selectedProduct,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, purchase, sale, stock ->
                    if (selectedProduct == null) {
                        viewModel.saveProduct(name, purchase, sale, stock)
                    } else {
                        viewModel.updateProduct(
                            selectedProduct!!.copy(
                                name = name,
                                purchasePrice = purchase,
                                salePrice = sale,
                                stock = stock
                            )
                        )
                    }
                    showAddDialog = false
                }
            )
        }

        // DIÁLOGO PARA CONFIRMAR ELIMINACIÓN
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("¿Eliminar producto?") },
                text = { Text("¿Estás seguro de que deseas eliminar a ${productToDelete?.name}? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            productToDelete?.let { viewModel.deleteProduct(it) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {  }
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF1976D2))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                    }
                }
            }
            Text(
                text = "Venta: $${product.salePrice} | Stock: ${product.stock}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun AddProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var purchasePrice by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "") }
    var salePrice by remember { mutableStateOf(product?.salePrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("Precio Compra") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = { salePrice = it },
                    label = { Text("Precio Venta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        purchasePrice.toDoubleOrNull() ?: 0.0,
                        salePrice.toDoubleOrNull() ?: 0.0,
                        stock.toIntOrNull() ?: 0
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}