package com.example.project1.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project1.data.local.dao.CustomerDao
import com.example.project1.data.local.dao.ProductDao
import com.example.project1.data.local.dao.SaleItemDao
import com.example.project1.data.local.dao.SalesDao
import com.example.project1.data.local.entities.Customer
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Product
import com.example.project1.data.local.enums.PaymentMethod
import kotlinx.coroutines.launch
import com.example.project1.data.local.entities.Sale
import com.example.project1.data.local.entities.SaleItem
import com.example.project1.data.local.enums.SaleStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class POSViewModel(
    private val customerDao: CustomerDao,
    private val productDao: ProductDao,
    private val salesDao: SalesDao,
    private val saleItemDao: SaleItemDao
) : ViewModel() {

    // --- ESTADOS PARA UI (Formularios) ---
    var customerName by mutableStateOf("")
    var customerPhone by mutableStateOf("")

    // --- FLUJOS DE DATOS (Observables por la UI) ---
    val customers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val availableProducts: Flow<List<Product>> = productDao.getAvailableProducts()
    val allSales: Flow<List<Sale>> = salesDao.getAllSales()

    // --- MÓDULO DE CLIENTES ---
    fun saveCustomer(name: String, phone: String) {
        viewModelScope.launch {
            customerDao.insert(Customer(name = name, phone = phone))
            // Limpiar campos si se usan los estados internos
            customerName = ""
            customerPhone = ""
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            customerDao.update(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerDao.delete(customer)
        }
    }

    // --- MÓDULO DE PRODUCTOS ---
    fun saveProduct(name: String, purchase: Double, sale: Double, stock: Int) {
        viewModelScope.launch {
            val newProduct = Product(
                name = name,
                purchasePrice = purchase,
                salePrice = sale,
                stock = stock
            )
            productDao.insert(newProduct)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productDao.updateProductStock(product) // Usa el método update de tu DAO
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.delete(product) // Asegúrate de tener 'delete' en tu ProductDao
        }
    }


    //View sale
    // Lista temporal para el carrito
    // --- DENTRO DE POSViewModel.kt ---

    // Usamos un Map para que sea más fácil gestionar Producto -> Cantidad
    var cartItems by mutableStateOf<Map<Product, Int>>(emptyMap())
        private set

    /**
     * Agrega un producto o incrementa su cantidad validando el stock disponible.
     */
    fun addToCart(product: Product) {
        val currentQty = cartItems[product] ?: 0
        // Validación: No permitir agregar más de lo que hay en stock
        if (currentQty < product.stock) {
            cartItems = cartItems + (product to currentQty + 1)
        }
    }

    /**
     * Reduce la cantidad de un producto. Si llega a 0, lo elimina del carrito.
     */
    fun removeFromCart(product: Product) {
        val currentQty = cartItems[product] ?: 0
        if (currentQty > 1) {
            cartItems = cartItems + (product to currentQty - 1)
        } else {
            deleteFromCart(product)
        }
    }

    /**
     * Elimina por completo el producto del carrito (botón basura).
     */
    fun deleteFromCart(product: Product) {
        cartItems = cartItems - product
    }

    fun clearCart() {
        cartItems = emptyMap()
    }

    /**
     * Finaliza la venta, guarda en la DB y descuenta el stock real.
     */
// --- CORRECCIÓN EN POSViewModel.kt ---
    fun finalizeSale(customer: Customer, initialPaymentAmount: Double, method: PaymentMethod) {
        viewModelScope.launch {
            try {
                val total = cartItems.entries.sumOf { it.key.salePrice * it.value }

                // 1. Crear e insertar la Venta (Encabezado)
                val newSale = Sale(
                    customerId = customer.id,
                    saleTotal = total,
                    remainingBalance = (total - initialPaymentAmount).coerceAtLeast(0.0),
                    status = if ((total - initialPaymentAmount) <= 0) SaleStatus.COMPLETED else SaleStatus.PENDING
                )
                val saleId = salesDao.insertSale(newSale)

                // 2. GUARDAR DETALLES (Lo nuevo: ¿Qué productos se llevó?)
                cartItems.forEach { (product, quantity) ->
                    val detail = SaleItem(
                        saleId = saleId,
                        productId = product.id,
                        quantity = quantity,
                        priceAtSale = product.salePrice // Guardamos el precio actual
                    )
                    salesDao.insertSaleItem(detail)

                    // Aprovechamos el ciclo para descontar el stock
                    val updatedProduct = product.copy(stock = product.stock - quantity)
                    productDao.updateProduct(updatedProduct)
                }

                // 3. Registrar el pago inicial si existe
                if (initialPaymentAmount > 0) {
                    val payment = Payment(
                        saleId = saleId,
                        amount = initialPaymentAmount,
                        paymentMethod = method,
                        date = System.currentTimeMillis()
                    )
                    salesDao.insertPayment(payment)
                }

                clearCart()

            } catch (e: Exception) {
                android.util.Log.e("POS_ERROR", "Error: ${e.message}")
            }
        }
    }

    // En tu POSViewModel.kt
    fun getPaymentsBySale(saleId: Long) = salesDao.getPaymentsBySale(saleId)
    fun getItemsBySale(saleId: Long) = salesDao.getItemsBySale(saleId)

    // Función para registrar abono (ya la teníamos, asegúrate de que use la transacción)
    fun addPayment(saleId: Long, amount: Double, method: PaymentMethod) {
        viewModelScope.launch {
            val payment = Payment(
                saleId = saleId,
                amount = amount,
                paymentMethod = method,
                date = System.currentTimeMillis()
            )
            salesDao.registerPaymentTransaction(saleId, amount, payment)
        }
    }


    // PaymentCollectionScreen
    // --- POSViewModel.kt ---
// --- En POSViewModel.kt ---

    val pendingSales = salesDao.getPendingSales()

    // En POSViewModel.kt
    val allPayments: StateFlow<List<Payment>> = salesDao.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}
