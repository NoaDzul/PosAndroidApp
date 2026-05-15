package com.example.project1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.project1.data.local.dao.CustomerDao
import com.example.project1.data.local.dao.PaymentDao
import com.example.project1.data.local.dao.ProductDao
import com.example.project1.data.local.dao.ReserveDao
import com.example.project1.data.local.entities.Customer
import com.example.project1.data.local.entities.InventoryMovement
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Product
import com.example.project1.data.local.entities.Reserve
import com.example.project1.data.local.enums.Converters

@Database(
    entities = [
        Customer::class,
        Product::class,
        Payment::class,
        Reserve::class,
        InventoryMovement::class

    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao
    abstract fun reserveDao():ReserveDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
}