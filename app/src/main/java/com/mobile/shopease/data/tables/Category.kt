package com.mobile.shopease.data.tables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)