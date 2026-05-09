package com.example.project1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToNewSale: () -> Unit,
    onNavigateToCompletedSales: () -> Unit,
) {
    // Definimos un degradado suave para el fondo o el encabezado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
    )

    Scaffold(
        containerColor = Color(0xFFF5F7FA) // Fondo gris muy claro y limpio
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- ENCABEZADO MODERNO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(gradient, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "Bienvenido,",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Gestor de Ventas",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- CUADRÍCULA DE OPCIONES ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    MenuCard(
                        title = "Nueva Venta",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFF673AB7), // Púrpura
                        onClick = onNavigateToNewSale
                    )
                }
                item {
                    MenuCard(
                        title = "Cobranza",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF2E7D32), // Verde
                        onClick = onNavigateToPayments
                    )
                }
                item {
                    MenuCard(
                        title = "Productos",
                        icon = Icons.Default.List,
                        color = Color(0xFFF57C00), // Naranja
                        onClick = onNavigateToProducts
                    )
                }
                item {
                    MenuCard(
                        title = "Clientes",
                        icon = Icons.Default.Person,
                        color = Color(0xFF0288D1), // Azul
                        onClick = onNavigateToCustomers
                    )
                }
                item {
                    MenuCard(
                        title = "Historial",
                        icon = Icons.Default.Menu,
                        color = Color(0xFF455A64), // Gris Azulado
                        onClick = onNavigateToCompletedSales
                    )
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Círculo de fondo para el icono
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    tint = color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                ),
                color = Color(0xFF263238)
            )
        }
    }
}