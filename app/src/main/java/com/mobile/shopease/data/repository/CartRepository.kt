package com.mobile.shopease.data.repository

import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.tables.CartItem
import com.mobile.shopease.data.tables.PromoCode
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class CartRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private fun currentUserId(): String {
        return SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: throw Exception("User not authenticated")
    }

    // Fetches the cart with joined product info
    suspend fun getCartItems(): List<CartItem> {
        val userId = currentUserId()
        return postgrest["cart_items"]
            .select(Columns.raw("*, products(*)")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<CartItem>()
    }

    // Adds a product (or increases its quantity if it already exists)
    suspend fun addToCart(productId: String, quantity: Int = 1) {
        val userId = currentUserId()

        val existing = postgrest["cart_items"]
            .select(Columns.raw("quantity")) {
                filter {
                    eq("user_id", userId)
                    eq("product_id", productId)
                }
            }
            .decodeSingleOrNull<JsonObject>()

        if (existing != null) {
            updateQuantity(productId, (existing["quantity"]?.jsonPrimitive?.int ?: 0) + quantity)
        } else {
            postgrest["cart_items"].insert(
                buildJsonObject {
                    put("user_id", userId)
                    put("product_id", productId)
                    put("quantity", quantity)
                }
            )
        }
    }

    // Updates the quantity (removes if it falls to 0)
    suspend fun updateQuantity(productId: String, quantity: Int) {
        val userId = currentUserId()

        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        postgrest["cart_items"]
            .update(buildJsonObject { put("quantity", quantity) }) {
                filter {
                    eq("user_id", userId)
                    eq("product_id", productId)
                }
            }
    }

    // Removes an item from the cart
    suspend fun removeFromCart(productId: String) {
        val userId = currentUserId()
        postgrest["cart_items"]
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("product_id", productId)
                }
            }
    }

    // Clears the cart completely (useful after checkout)
    suspend fun clearCart() {
        val userId = currentUserId()
        postgrest["cart_items"]
            .delete {
                filter { eq("user_id", userId) }
            }
    }

    suspend fun getPromoCode(code: String): PromoCode? {
        val promo = postgrest["promo_codes"]
            .select() {
                filter {
                    eq("code", code.uppercase())
                    eq("is_active", true)
                }
            }
            .decodeSingleOrNull<PromoCode>() ?: return null

        // Check expiration
        promo.expiresAt?.let {
            try {
                val expiry = Instant.parse(it)
                if (expiry < Clock.System.now()) return null
            } catch (e: Exception) {
                // If date parsing fails, accept it or log it
            }
        }

        return promo
    }
}