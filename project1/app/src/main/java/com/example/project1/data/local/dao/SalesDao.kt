package com.example.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Sale
import com.example.project1.data.local.entities.SaleItem
import com.example.project1.data.local.enums.SaleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {

    // 1. CAMBIO IMPORTANTE: Quita el REPLACE.
    // Para insertar una venta nueva usa IGNORE o ABORT (por defecto).
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :saleId")
    fun getSaleById(saleId: Long): Sale?

    @Query("SELECT * FROM sales WHERE remainingBalance > 0 ORDER BY id DESC")
    fun getPendingSales(): Flow<List<Sale>>

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE saleId = :saleId ORDER BY id DESC")
    fun getPaymentsBySale(saleId: Long): Flow<List<Payment>>

    /**
     * Esta función es la que debes llamar desde el ViewModel al finalizar la venta
     * para que todo ocurra en un solo bloque atómico.
     */
    @Transaction
    suspend fun insertSaleWithInitialPayment(sale: Sale, payment: Payment?) {
        val saleId = insertSale(sale) // Insertamos la venta primero

        // Si hay un pago inicial, le asignamos el ID que Room nos devolvió
        payment?.let {
            insertPayment(it.copy(saleId = saleId))
        }
    }

    @Transaction
    suspend fun registerPaymentTransaction(saleId: Long, amountPaid: Double, payment: Payment) {
        val sale = getSaleById(saleId) ?: return
        val newBalance = (sale.remainingBalance - amountPaid).coerceAtLeast(0.0)
        val newStatus = if (newBalance <= 0.0) SaleStatus.COMPLETED else SaleStatus.PENDING

        insertPayment(payment)

        val updatedSale = sale.copy(
            remainingBalance = newBalance,
            status = newStatus
        )
        updateSale(updatedSale)
    }


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSaleItem(saleItem: SaleItem)

    @Query("""
        SELECT * FROM sale_items 
        WHERE saleId = :saleId
    """)
    fun getItemsBySale(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM payments")
    fun getAllPayments(): Flow<List<Payment>>

}