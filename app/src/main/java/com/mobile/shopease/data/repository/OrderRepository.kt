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

    suspend fun getOrderById(orderId: String): Order {
        return SupabaseClient.client.postgrest["orders"]
            .select { filter { eq("id", orderId) } }
            .decodeSingle<Order>()
    }

    suspend fun getOrderItems(orderId: String): List<OrderItem> {
        return SupabaseClient.client.postgrest["order_items"]
            .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, products(*)")) {
                filter { eq("order_id", orderId) }
            }
            .decodeList<OrderItem>()
    }

    suspend fun cancelOrder(orderId: String) {
        val order = getOrderById(orderId)

        // Only allow cancellation for pending or confirmed orders
        if (order.status !in listOf("pending", "confirmed")) {
            throw Exception("Cannot cancel order with status: ${order.status}")
        }

        // Update the order status to cancelled
        SupabaseClient.client.postgrest["orders"]
            .update({
                set("status", "cancelled")
            }) {
                filter { eq("id", orderId) }
            }
    }
}