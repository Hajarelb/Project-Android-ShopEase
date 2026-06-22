package com.mobile.shopease.data.tables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromoCode(
    val id: String? = null,
    val code: String,
    @SerialName("discount_percent") val discountPercent: Double,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("expires_at") val expiresAt: String? = null
)
