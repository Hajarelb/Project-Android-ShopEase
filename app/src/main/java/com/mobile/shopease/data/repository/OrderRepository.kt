package com.mobile.shopease.data.repository

import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.tables.CartItem
import com.mobile.shopease.data.tables.Order
import com.mobile.shopease.data.tables.OrderItem
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest

class OrderRepository {

    private fun currentUserId(): String =
        SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: throw Exception("User not authenticated")

    suspend fun createOrder(
        items: List<CartItem>,
        total: Double,
        paymentMethod: String   // "cod" or "online"
    ): String {
        val userId = currentUserId()
        val paymentStatus = if (paymentMethod == "online") "paid" else "pending"

        val order = SupabaseClient.client.postgrest["orders"]
            .insert(
                Order(
                    userId = userId,
                    total = total,
                    paymentMethod = paymentMethod,
                    paymentStatus = paymentStatus
                )
            ) { select() }
            .decodeSingle<Order>()

        val orderId = order.id ?: throw Exception("Order creation failed")

        val orderItems = items.map {
            OrderItem(
                orderId = orderId,
                productId = it.productId,
                quantity = it.quantity,
                unitPrice = it.products?.price ?: 0.0
            )
        }

        SupabaseClient.client.postgrest["order_items"].insert(orderItems)

        return orderId
    }

    suspend fun getUserOrders(): List<Order> {
        val userId = currentUserId()
        return SupabaseClient.client.postgrest["orders"]
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<Order>()
    }
}