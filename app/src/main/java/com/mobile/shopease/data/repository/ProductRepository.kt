package com.mobile.shopease.data.repository

import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.tables.Category
import com.mobile.shopease.data.tables.Product
import com.mobile.shopease.data.tables.Review
import com.mobile.shopease.data.tables.WishlistItem
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.gotrue.auth
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProductRepository {

    private val db get() = SupabaseClient.client.postgrest
    private val auth get() = SupabaseClient.client.auth

    // ── Products ─────────────────────────────────────────────────────────────

    suspend fun getProducts(categoryId: String? = null): List<Product> {
        return db["products"].select(
            columns = Columns.raw("*, product_ratings(avg_rating, review_count)")
        ) {
            filter {
                eq("is_active", true)
                if (categoryId != null) eq("category_id", categoryId)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<Product>()
    }

    suspend fun getFeaturedProducts(): List<Product> {
        return db["products"].select(
            columns = Columns.raw("*, product_ratings(avg_rating, review_count)")
        ) {
            filter {
                eq("is_active", true)
                eq("is_featured", true)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<Product>()
    }

    suspend fun getProductById(id: String): Product {
        return db["products"].select(
            columns = Columns.raw("*, product_ratings(avg_rating, review_count)")
        ) {
            filter { eq("id", id) }
            limit(1)
            single()
        }.decodeAs<Product>()
    }

    // ── Categories ───────────────────────────────────────────────────────────

    suspend fun getCategories(): List<Category> {
        return db["categories"].select {
            order("name", Order.ASCENDING)
        }.decodeList<Category>()
    }

    // ── Reviews ──────────────────────────────────────────────────────────────

    suspend fun getReviews(productId: String): List<Review> {
        return db["reviews"].select {
            filter { eq("product_id", productId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<Review>()
    }

    suspend fun addReview(productId: String, rating: Int, comment: String) {
        val userId = auth.currentUserOrNull()?.id
            ?: error("Must be authenticated to review")
        db["reviews"].upsert(
            buildJsonObject {
                put("product_id", productId)
                put("user_id", userId)
                put("rating", rating)
                put("comment", comment)
            },
            onConflict = "product_id,user_id"   // update if already reviewed
        )
    }

    // ── Wishlist ─────────────────────────────────────────────────────────────

    suspend fun saveProduct(productId: String) {
        val userId = auth.currentUserOrNull()?.id
            ?: error("Must be authenticated to save products")
        db["wishlists"].insert(
            buildJsonObject {
                put("user_id", userId)
                put("product_id", productId)
            }
        )
    }

    suspend fun unsaveProduct(productId: String) {
        val userId = auth.currentUserOrNull()?.id
            ?: error("Must be authenticated")
        db["wishlists"].delete {
            filter {
                eq("user_id", userId)
                eq("product_id", productId)
            }
        }
    }

    suspend fun getSavedProducts(): List<Product> {
        val userId = auth.currentUserOrNull()?.id
            ?: error("Must be authenticated")
        val items = db["wishlists"].select(
            columns = Columns.raw("*, products(*, product_ratings(avg_rating, review_count))")
        ) {
            filter { eq("user_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<WishlistItem>()
        return items.mapNotNull { it.products }
    }

suspend fun getSavedProductIds(): Set<String> {
    val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return emptySet()
    val items = SupabaseClient.client.postgrest["wishlists"]
        .select() // select ALL columns, not just product_id to prevent serialization issues
        {
            filter {
                eq("user_id", userId)
            }
        }
        .decodeList<WishlistItem>()
    return items.map { it.productId }.toSet()
}
}