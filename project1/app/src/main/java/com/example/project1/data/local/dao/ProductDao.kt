package com.example.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.project1.data.local.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(product: Product)

    @Query("SELECT * FROM products WHERE stock > 0")
    fun getAvailableProducts(): Flow<List<Product>>

    @Update
    suspend fun updateProductStock(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Update
    suspend fun updateProduct(product: Product)
}