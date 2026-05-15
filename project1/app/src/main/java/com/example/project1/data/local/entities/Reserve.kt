package com.example.project1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reserves")
data class Reserve(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val productId: Long,
    val quantity: Int,
    val totalAmount: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
)