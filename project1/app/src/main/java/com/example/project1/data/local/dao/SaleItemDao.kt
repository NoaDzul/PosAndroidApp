package com.example.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {

    @Query(
        """
    SELECT products.name FROM products 
    INNER JOIN sale_items ON products.id = sale_items.productId 
    WHERE sale_items.saleId = :saleId
"""
    )
    fun getProductsBySale(saleId: Long): Flow<List<String>>
}