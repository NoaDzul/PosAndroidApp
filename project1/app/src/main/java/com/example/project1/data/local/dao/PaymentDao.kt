package com.example.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.project1.data.local.entities.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    // Obtener todos los pagos de un cliente específico (Historial)
    @Query("SELECT * FROM payments WHERE clientId = :clientId ORDER BY date DESC")
    fun getPaymentsByCustomer(clientId: Long): Flow<List<Payment>>

    // Sumatoria de abonos para calcular el saldo a favor del cliente
    @Query("SELECT SUM(amount) FROM payments WHERE clientId = :clientId")
    fun getTotalAbonadoByCustomer(clientId: Long): Flow<Double?>

    // Opcional: Obtener todos los pagos del día (para el cierre de caja)
    @Query("SELECT * FROM payments WHERE date >= :startOfDay")
    fun getPaymentsOfDay(startOfDay: Long): Flow<List<Payment>>
}