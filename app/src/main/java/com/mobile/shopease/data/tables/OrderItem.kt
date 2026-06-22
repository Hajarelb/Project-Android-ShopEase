package com.mobile.shopease.data.tables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val id: String? = null,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    val products: Product? = null
)