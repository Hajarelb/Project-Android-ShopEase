package com.mobile.shopease.data.tables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Int,
    @SerialName("created_at") val createdAt: String? = null,
    val products: Product? = null   // fetched via Postgrest join
)