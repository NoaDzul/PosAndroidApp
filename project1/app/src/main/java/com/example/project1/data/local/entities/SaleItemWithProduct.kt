package com.example.project1.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class SaleItemWithProduct(
    @Embedded val saleItem: SaleItem,
    @Relation(
        parentColumn = "productId", // Columna en SaleItem
        entityColumn = "id"         // Columna en Product
    )
    val product: Product
)