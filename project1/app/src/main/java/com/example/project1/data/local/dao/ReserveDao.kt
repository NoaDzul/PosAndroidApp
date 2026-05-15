package com.example.project1.data.local.dao

import androidx.room.*
import com.example.project1.data.local.entities.Reserve
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.models.ReservationWithClient
import kotlinx.coroutines.flow.Flow

@Dao
interface ReserveDao {

    // --- RESERVAS ---

    @Insert
    suspend fun insertReserve(reserve: Reserve)

    @Delete
    suspend fun deleteReserve(reserve: Reserve)

    @Query("""
        SELECT 
            reserves.id AS reserveId, 
            customers.id AS clientId, 
            customers.name AS clientName, 
            reserves.quantity AS quantity,
            (reserves.quantity * products.salePrice) AS totalAmount
        FROM reserves
        INNER JOIN customers ON reserves.clientId = customers.id
        INNER JOIN products ON reserves.productId = products.id
        WHERE reserves.productId = :productId
    """)
    fun getReservationsByProduct(productId: Long): Flow<List<ReservationWithClient>>

    // --- PAGOS / ABONOS ---

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Query("SELECT SUM(amount) FROM payments WHERE clientId = :clientId")
    fun getCustomerTotalPayments(clientId: Long): Flow<Double?>

    @Query("SELECT * FROM payments WHERE clientId = :clientId ORDER BY date DESC")
    fun getPaymentsByClient(clientId: Long): Flow<List<Payment>>
}