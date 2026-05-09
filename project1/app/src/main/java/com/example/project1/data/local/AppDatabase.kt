package com.example.project1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.project1.data.local.dao.CustomerDao
import com.example.project1.data.local.dao.ProductDao
import com.example.project1.data.local.dao.SaleItemDao
import com.example.project1.data.local.dao.SalesDao
import com.example.project1.data.local.entities.Customer
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Product
import com.example.project1.data.local.entities.Sale
import com.example.project1.data.local.entities.SaleItem
import com.example.project1.data.local.enums.Converters

@Database(
    entities = [
        Customer::class,
        Product::class,
        Sale::class,
        SaleItem::class,
        Payment::class,

    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun salesDao(): SalesDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
}