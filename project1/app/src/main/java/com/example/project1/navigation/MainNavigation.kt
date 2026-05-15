package com.example.project1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.project1.ui.screens.*
import com.example.project1.ui.viewmodel.POSViewModel

@Composable
fun MainNavigation(viewModel: POSViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "menu") {
        // 1. Menú Principal
        composable("menu") {
            MainMenuScreen(
                onNavigateToCustomers = { navController.navigate("customers") },
                onNavigateToProducts = { navController.navigate("products") }
            )
        }

        // 2. Módulo de Clientes (CRUD)
        composable("customers") {
            CustomerCrudScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

// 3. Módulo de Productos
        composable("products") {
            ProductCrudScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
//        // 4. Detalle de Reservas por Producto (Nueva Pantalla)
//        composable(
//            route = "product_detail/{productId}",
//            arguments = listOf(navArgument("productId") { type = NavType.LongType })
//        ) { backStackEntry ->
//            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
//            ProductReservationDetailScreen(viewModel, productId)
//        }
    }
}