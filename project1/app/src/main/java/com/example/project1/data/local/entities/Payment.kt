package com.example.project1.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.project1.data.local.enums.PaymentMethod


@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod,
    val reference: String? = null
)
