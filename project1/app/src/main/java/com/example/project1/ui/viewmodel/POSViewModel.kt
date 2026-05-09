package com.example.project1.ui.viewmodel

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
import com.example.project1.data.local.entities.Sale
import com.example.project1.data.local.entities.SaleItem
import com.example.project1.data.local.enums.PaymentMethod
import com.example.project1.data.local.enums.SaleStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class POSViewModel(
    private val customerDao: CustomerDao,
    private val productDao: ProductDao,
    private val salesDao: SalesDao,
    private val saleItemDao: SaleItemDao
) : ViewModel() {

    // --- ESTADOS PARA UI ---
    var customerName by mutableStateOf("")
    var customerPhone by mutableStateOf("")

    // --- FLUJOS DE DATOS ---
    val customers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val availableProducts: Flow<List<Product>> = productDao.getAvailableProducts()
    val allSales: Flow<List<Sale>> = salesDao.getAllSales()
    val pendingSales = salesDao.getPendingSales()
    val allPayments: StateFlow<List<Payment>> = salesDao.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CARRITO ---
    var cartItems by mutableStateOf<Map<Product, Int>>(emptyMap())
        private set

    fun addToCart(product: Product) {
        val currentQty = cartItems[product] ?: 0
        if (currentQty < product.stock) {
            cartItems = cartItems + (product to currentQty + 1)
        }
    }

    fun removeFromCart(product: Product) {
        val currentQty = cartItems[product] ?: 0
        if (currentQty > 1) {
            cartItems = cartItems + (product to currentQty - 1)
        } else {
            deleteFromCart(product)
        }
    }

    fun deleteFromCart(product: Product) {
        cartItems = cartItems - product
    }

    fun clearCart() {
        cartItems = emptyMap()
    }

    // --- LÓGICA DE VENTA ACUMULADA ---
    fun finalizeSale(customer: Customer, initialPaymentAmount: Double, method: PaymentMethod) {
        viewModelScope.launch {
            try {
                val totalCart = cartItems.entries.sumOf { it.key.salePrice * it.value }

                // 1. Buscar si el cliente ya tiene una deuda abierta
                val existingSale = salesDao.getActiveDebtByCustomer(customer.id)

                val saleId: Long
                if (existingSale != null) {
                    // ACTUALIZAR VENTA EXISTENTE
                    saleId = existingSale.id
                    val newTotal = existingSale.saleTotal + totalCart
                    val newBalance = existingSale.remainingBalance + (totalCart - initialPaymentAmount)

                    val updatedSale = existingSale.copy(
                        saleTotal = newTotal,
                        remainingBalance = newBalance.coerceAtLeast(0.0),
                        status = if (newBalance <= 0) SaleStatus.COMPLETED else SaleStatus.PENDING
                    )
                    salesDao.updateSale(updatedSale)
                } else {
                    // CREAR NUEVA VENTA
                    val newSale = Sale(
                        customerId = customer.id,
                        saleTotal = totalCart,
                        remainingBalance = (totalCart - initialPaymentAmount).coerceAtLeast(0.0),
                        status = if ((totalCart - initialPaymentAmount) <= 0) SaleStatus.COMPLETED else SaleStatus.PENDING,
                        date = System.currentTimeMillis()
                    )
                    saleId = salesDao.insertSale(newSale)
                }

                // 2. PROCESAR PRODUCTOS (Sumar a la deuda)
                cartItems.forEach { (product, quantity) ->
                    val existingItem = salesDao.getItemInSale(saleId, product.id)

                    if (existingItem != null) {
                        // El producto ya estaba en la deuda, actualizamos cantidad
                        val updatedItem = existingItem.copy(
                            quantity = existingItem.quantity + quantity
                        )
                        salesDao.updateSaleItem(updatedItem)
                    } else {
                        // Producto nuevo para esta deuda
                        val detail = SaleItem(
                            saleId = saleId,
                            productId = product.id,
                            quantity = quantity,
                            priceAtSale = product.salePrice
                        )
                        salesDao.insertSaleItem(detail)
                    }

                    // Descontar stock real
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
                android.util.Log.e("POS_ERROR", "Error en finalizeSale: ${e.message}")
            }
        }
    }

    // --- OTROS MÉTODOS ---
    fun saveCustomer(name: String, phone: String) {
        viewModelScope.launch { customerDao.insert(Customer(name = name, phone = phone)) }
    }
    // --- DENTRO DE POSViewModel.kt ---

    /**
     * Actualiza los datos de un cliente existente (Nombre y Teléfono).
     */
    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                customerDao.update(customer)
            } catch (e: Exception) {
                android.util.Log.e("POS_ERROR", "Error al actualizar cliente: ${e.message}")
            }
        }
    }

    /**
     * Elimina un cliente de la base de datos.
     */
    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                customerDao.delete(customer)
            } catch (e: Exception) {
                android.util.Log.e("POS_ERROR", "Error al eliminar cliente: ${e.message}")
            }
        }
    }

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

    // --- DENTRO DE POSViewModel.kt ---

    /**
     * Guarda un nuevo producto en el inventario.
     */
    fun saveProduct(name: String, purchase: Double, sale: Double, stock: Int) {
        viewModelScope.launch {
            try {
                val newProduct = Product(
                    name = name,
                    purchasePrice = purchase,
                    salePrice = sale,
                    stock = stock
                )
                productDao.insert(newProduct)
            } catch (e: Exception) {
                android.util.Log.e("POS_ERROR", "Error al guardar producto: ${e.message}")
            }
        }
    }

    fun getPaymentsBySale(saleId: Long) = salesDao.getPaymentsBySale(saleId)
    fun getItemsBySale(saleId: Long) = salesDao.getItemsBySale(saleId)

    // Métodos faltantes de productos para que no de error
    fun updateProduct(product: Product) = viewModelScope.launch { productDao.updateProduct(product) }
    fun deleteProduct(product: Product) = viewModelScope.launch { productDao.delete(product) }
    // Dentro de POSViewModel
    fun getItemsWithProductBySale(saleId: Long) = salesDao.getItemsWithProductBySale(saleId)

    // Dentro de la clase POSViewModel
    val completedSales: Flow<List<Sale>> = salesDao.getCompletedSales()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

}