package com.example.project1.data.local.models

data class ReservationWithClient(
    val reserveId: Long,
    val clientId: Long,
    val clientName: String,
    val quantity: Int,
    val totalAmount: Double?
)