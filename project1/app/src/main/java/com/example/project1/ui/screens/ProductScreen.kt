package com.example.project1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.project1.data.local.entities.Product
import com.example.project1.ui.viewmodel.POSViewModel


@Composable
fun ProductCrudScreen(viewModel: POSViewModel) {
    val products by viewModel.availableProducts.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedProduct = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Gestión de Productos", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onEdit = {
                            selectedProduct = product
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }

        if (showDialog) {
            AddProductDialog(
                product = selectedProduct,
                onDismiss = { showDialog = false },
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
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Blue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }
                }
            }
            Text(text = "Venta: $${product.salePrice} | Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Precio Compra") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Precio Venta") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    name,
                    purchasePrice.toDoubleOrNull() ?: 0.0,
                    salePrice.toDoubleOrNull() ?: 0.0,
                    stock.toIntOrNull() ?: 0
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}