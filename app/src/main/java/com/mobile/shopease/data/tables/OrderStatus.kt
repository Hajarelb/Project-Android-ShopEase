package com.mobile.shopease.data.tables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class OrderStatusEnum(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    PENDING("Pending", Color(0xFFFFA500), Icons.Outlined.HourglassEmpty),
    CONFIRMED("Confirmed", Color(0xFF2196F3), Icons.Outlined.CheckCircle),
    SHIPPED("Shipped", Color(0xFF9C27B0), Icons.Outlined.LocalShipping),
    DELIVERED("Delivered", Color(0xFF4CAF50), Icons.Outlined.TaskAlt),
    CANCELLED("Cancelled", Color(0xFFF44336), Icons.Outlined.Cancel);

    companion object {
        fun fromString(status: String?): OrderStatusEnum {
            return when (status?.lowercase()) {
                "pending" -> PENDING
                "confirmed" -> CONFIRMED
                "shipped" -> SHIPPED
                "delivered" -> DELIVERED
                "cancelled" -> CANCELLED
                else -> PENDING
            }
        }
    }
}
