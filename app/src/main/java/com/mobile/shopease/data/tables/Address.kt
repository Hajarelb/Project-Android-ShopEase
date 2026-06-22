package com.mobile.shopease.data.tables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val street: String,
    val city: String,
    val state: String,
    @SerialName("postal_code")
    val postalCode: String,
    val country: String,
    @SerialName("is_default")
    val isDefault: Boolean = false
)
