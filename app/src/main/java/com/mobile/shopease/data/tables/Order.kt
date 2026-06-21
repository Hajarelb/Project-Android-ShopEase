package com.mobile.shopease.data.tables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val total: Double,
    val status: String = "pending",
    @SerialName("payment_method") val paymentMethod: String,   // "cod" or "online"
    @SerialName("payment_status") val paymentStatus: String,   // "pending" or "paid"
    @SerialName("created_at") val createdAt: String? = null
)