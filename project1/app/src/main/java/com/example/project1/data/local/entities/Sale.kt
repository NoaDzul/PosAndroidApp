package com.example.project1.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.project1.data.local.enums.SaleStatus

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val saleTotal: Double,
    val remainingBalance: Double,
    val status: SaleStatus = SaleStatus.PENDING
)