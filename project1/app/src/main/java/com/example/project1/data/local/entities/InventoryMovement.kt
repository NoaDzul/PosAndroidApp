package com.example.project1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.project1.data.local.enums.InventoryMovementType

@Entity("inventory_movements")
data class InventoryMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val quantity: Int,
    val type: InventoryMovementType
)