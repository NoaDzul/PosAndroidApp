package com.example.project1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project1.data.local.dao.CustomerDao
import com.example.project1.data.local.dao.PaymentDao
import com.example.project1.data.local.dao.ProductDao
import com.example.project1.data.local.dao.ReserveDao
import com.example.project1.data.local.entities.Customer
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Product
import com.example.project1.data.local.entities.Reserve
import com.example.project1.data.local.enums.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class POSViewModel(
    private val customerDao: CustomerDao,
    private val productDao: ProductDao,
    private val reserveDao:ReserveDao,
    private val paymentDao:PaymentDao
) : ViewModel() {

    val customers: Flow<List<Customer>> = customerDao.getAllCustomers()
    fun saveCustomer(name: String, phone: String) {
        viewModelScope.launch {
            val newCustomer = Customer(name = name, phone = phone)
            customerDao.insert(newCustomer)
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

    val availableProducts: Flow<List<Product>> = productDao.getAllProducts()

    // Guardar Producto
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

    // Actualizar Producto
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productDao.updateProduct(product)
        }
    }

    // Eliminar Producto
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.delete(product)
        }
    }

    // Agrega esto a tu POSViewModel
    fun processReservationWithPayment(
        customer: Customer,
        product: Product,
        quantity: Int,
        initialAmount: Double,
        method: PaymentMethod
    ) {
        viewModelScope.launch {
            // 1. Insertar la reserva
            val reserve = Reserve(
                clientId = customer.id,
                productId = product.id,
                quantity = quantity
            )
            reserveDao.insertReserve(reserve)

            // 2. Si el abono es mayor a cero, registrar el pago
            if (initialAmount > 0.0) {
                val payment = Payment(
                    clientId = customer.id,
                    amount = initialAmount,
                    paymentMethod = method,
                    reference = "Abono inicial reserva: ${product.name}"
                )
                paymentDao.insertPayment(payment)
            }
        }
    }
}