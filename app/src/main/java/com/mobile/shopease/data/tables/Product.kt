package com.mobile.shopease.data.tables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    @SerialName("original_price") val originalPrice: Double? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    // Joined from product_ratings view — nullable since not always joined
    @SerialName("avg_rating") val avgRating: Double? = null,
    @SerialName("review_count") val reviewCount: Int? = null,
)