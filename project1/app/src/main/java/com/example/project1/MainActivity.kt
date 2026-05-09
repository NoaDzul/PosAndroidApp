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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project1.data.local.DatabaseProvider
import com.example.project1.ui.navigation.MainNavigation
import com.example.project1.ui.viewmodel.POSViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DatabaseProvider.getDatabase(this)

        enableEdgeToEdge()

        setContent {
            val posViewModel: POSViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return POSViewModel(
                            customerDao = db.customerDao(),
                            productDao = db.productDao(),
                            salesDao = db.salesDao(),
                            saleItemDao = db.saleItemDao()
                        ) as T
                    }
                }
            )

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