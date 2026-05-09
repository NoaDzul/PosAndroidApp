package com.example.project1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.project1.ui.screens.CompletedSalesScreen
import com.example.project1.ui.screens.CustomerCrudScreen // Cambiado para usar el CRUD completo
import com.example.project1.ui.screens.MainMenuScreen
import com.example.project1.ui.screens.NewSaleScreen
import com.example.project1.ui.screens.PaymentCollectionScreen
import com.example.project1.ui.screens.ProductCrudScreen // Nueva importación
import com.example.project1.ui.viewmodel.POSViewModel

@Composable
fun MainNavigation(viewModel: POSViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "menu") {
        // 1. Menú Principal
        composable("menu") {
            MainMenuScreen(
                onNavigateToCustomers = { navController.navigate("customers") },
                onNavigateToProducts = { navController.navigate("products") },
                onNavigateToPayments = { navController.navigate("payments") },
                onNavigateToNewSale = { navController.navigate("new_sale") },
                onNavigateToCompletedSales = { navController.navigate("completed_sales") }
            )
        }

        // 2. Módulo de Clientes
        composable("customers") {
            CustomerCrudScreen(viewModel)
        }

        // 3. Módulo de Productos
        composable("products") {
            ProductCrudScreen(viewModel)
        }

        // 4. Módulo de Pagos
        composable("payments") {
            PaymentCollectionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Módulo de Pagos
        composable("new_sale") {
            NewSaleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Nueva pantalla de Historial
        composable("completed_sales") {
            CompletedSalesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}