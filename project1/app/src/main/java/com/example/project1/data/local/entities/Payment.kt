package com.example.project1.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.project1.data.local.enums.PaymentMethod

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod,
    val reference: String? = null
)
