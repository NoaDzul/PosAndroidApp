package com.example.project1.data.local.enums

import androidx.room.TypeConverter

class Converters {
    // Para PaymentMethod
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod) = value.name

    @TypeConverter
    fun toPaymentMethod(value: String) = enumValueOf<PaymentMethod>(value)

    // Para SaleStatus
    @TypeConverter
    fun fromSaleStatus(value: SaleStatus) = value.name

    @TypeConverter
    fun toSaleStatus(value: String) = enumValueOf<SaleStatus>(value)
}