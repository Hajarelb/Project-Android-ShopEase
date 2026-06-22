package com.mobile.shopease.data.repository

import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.tables.Category
import com.mobile.shopease.data.tables.Profile
import com.mobile.shopease.data.tables.Product
import com.mobile.shopease.data.tables.Review
import com.mobile.shopease.data.tables.WishlistItem
import kotlinx.serialization.Serializable
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

    suspend fun searchProducts(query: String): List<Product> {
        return db["products"].select(
            columns = Columns.raw("*, product_ratings(avg_rating, review_count)")
        ) {
            filter {
                eq("is_active", true)
                or {
                    ilike("name", "%$query%")
                    ilike("description", "%$query%")
                }
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<Product>()
    }

    // ── Categories ───────────────────────────────────────────────────────────

    suspend fun getCategories(): List<Category> {
        return db["categories"].select {
            order("name", Order.ASCENDING)
        }.decodeList<Category>()
    }

    // ── Reviews ──────────────────────────────────────────────────────────────

    suspend fun getReviews(productId: String): List<Review> {
        val reviews = db["reviews"].select {
            filter { eq("product_id", productId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<Review>()

        val profilesById = reviews
            .map { it.userId }
            .distinct()
            .associateWith { userId ->
                db["profiles"].select {
                    filter { eq("id", userId) }
                    limit(1)
                }.decodeList<Profile>().firstOrNull()
            }

        val currentUser = auth.currentUserOrNull()
        val currentUserName = currentUser?.let { user ->
            metadataDisplayName("full_name")
                ?: metadataDisplayName("name")
                ?: user.email?.substringBefore("@")
        }

        return reviews.map { review ->
            val reviewerName = profilesById[review.userId]
                ?.fullName
                ?.takeIf { it.isNotBlank() }
                ?: currentUserName.takeIf { currentUser?.id == review.userId }
            review.copy(reviewerName = reviewerName)
        }
    }

    suspend fun addReview(productId: String, rating: Int, comment: String) {
        val user = auth.currentUserOrNull()
            ?: error("Must be authenticated to review")
        if (!hasPurchasedProduct(productId)) {
            error("Only verified purchasers can review this product")
        }
        val reviewerName = metadataDisplayName("full_name")
            ?: metadataDisplayName("name")
            ?: user.email?.substringBefore("@")
        saveReviewerProfile(
            userId = user.id,
            fullName = reviewerName
        )
        db["reviews"].upsert(
            buildJsonObject {
                put("product_id", productId)
                put("user_id", user.id)
                put("rating", rating)
                put("comment", comment)
            },
            onConflict = "product_id,user_id"   // update if already reviewed
        )
    }

    suspend fun deleteReview(productId: String) {
        val userId = auth.currentUserOrNull()?.id
            ?: error("Must be authenticated to delete a review")
        db["reviews"].delete {
            filter {
                eq("product_id", productId)
                eq("user_id", userId)
            }
        }
    }

    suspend fun hasPurchasedProduct(productId: String): Boolean {
        val userId = auth.currentUserOrNull()?.id ?: return false
        val orders = db["orders"].select(columns = Columns.raw("id")) {
            filter { eq("user_id", userId) }
        }.decodeList<OrderId>()

        return orders.any { order ->
            db["order_items"].select(columns = Columns.raw("id")) {
                filter {
                    eq("order_id", order.id)
                    eq("product_id", productId)
                }
                limit(1)
            }.decodeList<OrderItemId>().isNotEmpty()
        }
    }

    @Serializable
    private data class OrderId(val id: String)

    @Serializable
    private data class OrderItemId(val id: String)


    private fun metadataDisplayName(key: String): String? {
        return auth.currentUserOrNull()
            ?.userMetadata
            ?.get(key)
            ?.toString()
            ?.trim('"')
            ?.takeIf { it.isNotBlank() }
    }

    private suspend fun saveReviewerProfile(userId: String, fullName: String?) {
        val name = fullName?.takeIf { it.isNotBlank() } ?: return
        runCatching {
            db["profiles"].upsert(
                buildJsonObject {
                    put("id", userId)
                    put("full_name", name)
                },
                onConflict = "id"
            )
        }
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