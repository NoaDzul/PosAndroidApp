package com.example.project1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project1.data.local.DatabaseProvider
import com.example.project1.navigation.MainNavigation
import com.example.project1.ui.viewmodel.POSViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Base de Datos y DAOs
        val db = DatabaseProvider.getDatabase(this)
        val customerDao = db.customerDao()
        val productDao = db.productDao()
        val reserveDao = db.reserveDao()
        val paymentDao = db.paymentDao()

        // Configurar el ViewModel con sus dependencias
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return POSViewModel(customerDao, productDao, reserveDao, paymentDao) as T
            }
        }
        val posViewModel = ViewModelProvider(this, viewModelFactory)[POSViewModel::class.java]

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(viewModel = posViewModel)
                }
            }
        }
    }
}