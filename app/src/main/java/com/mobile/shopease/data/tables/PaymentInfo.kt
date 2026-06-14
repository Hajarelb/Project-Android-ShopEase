package com.mobile.shopease.data.tables

data class PaymentInfo(
    val cardholderName: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = ""
)
