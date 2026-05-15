package com.example.project1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val stock: Int = 0
)
