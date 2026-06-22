package com.mobile.shopease.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.repository.ProductRepository
import com.mobile.shopease.data.tables.Category
import com.mobile.shopease.data.tables.Product
import com.mobile.shopease.data.tables.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Shared list state ────────────────────────────────────────────────────────

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val savedProductIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ── Detail state ──────────────────────────────────────────────────────────────

data class ProductDetailUiState(
    val product: Product? = null,
    val reviews: List<Review> = emptyList(),
    val isSaved: Boolean = false,
    val hasReviewed: Boolean = false,
    val canReview: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val error: String? = null,
    val reviewSuccess: Boolean = false,
)

// ── Wishlist state ────────────────────────────────────────────────────────────

data class WishlistUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ProductViewModel(
    private val repo: ProductRepository = ProductRepository()
) : ViewModel() {

    // ── List ─────────────────────────────────────────────────────────────────

    private val _listState = MutableStateFlow(ProductListUiState())
    val listState: StateFlow<ProductListUiState> = _listState.asStateFlow()

    fun loadProductList() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, error = null) }
            try {
                val categories = repo.getCategories()
                val products = repo.getProducts()
                val savedIds = repo.getSavedProductIds()
                _listState.update {
                    it.copy(
                        products = products,
                        categories = categories,
                        savedProductIds = savedIds,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _listState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        viewModelScope.launch {
            _listState.update { it.copy(selectedCategoryId = categoryId, searchQuery = "", isLoading = true) }
            try {
                val products = repo.getProducts(categoryId)
                _listState.update { it.copy(products = products, isLoading = false) }
            } catch (e: Exception) {
                _listState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _listState.update { it.copy(searchQuery = query, selectedCategoryId = null, isLoading = true) }
            try {
                val products = if (query.isBlank()) {
                    repo.getProducts()
                } else {
                    repo.searchProducts(query)
                }
                _listState.update { it.copy(products = products, isLoading = false) }
            } catch (e: Exception) {
                _listState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleSave(productId: String) {
        viewModelScope.launch {
            val saved = _listState.value.savedProductIds
            try {
                if (productId in saved) {
                    repo.unsaveProduct(productId)
                    _listState.update { it.copy(savedProductIds = saved - productId) }
                } else {
                    repo.saveProduct(productId)
                    _listState.update { it.copy(savedProductIds = saved + productId) }
                }
            } catch (e: Exception) {
                _listState.update { it.copy(error = e.message) }
            }
        }
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    private val _detailState = MutableStateFlow(ProductDetailUiState())
    val detailState: StateFlow<ProductDetailUiState> = _detailState.asStateFlow()

    fun loadProductDetail(productId: String, currentUserId: String?) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            try {
                val product = repo.getProductById(productId)
                val reviews = repo.getReviews(productId)
                val savedIds = repo.getSavedProductIds()
                val canReview = repo.hasPurchasedProduct(productId)
                _detailState.update {
                    it.copy(
                        product = product,
                        reviews = reviews,
                        isSaved = productId in savedIds,
                        hasReviewed = reviews.any { r -> r.userId == currentUserId },
                        canReview = canReview,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _detailState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleDetailSave(productId: String) {
        viewModelScope.launch {
            val isSaved = _detailState.value.isSaved
            try {
                if (isSaved) repo.unsaveProduct(productId) else repo.saveProduct(productId)
                _detailState.update { it.copy(isSaved = !isSaved) }
            } catch (e: Exception) {
                _detailState.update { it.copy(error = e.message) }
            }
        }
    }

    fun submitReview(productId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isSubmittingReview = true, error = null) }
            try {
                repo.addReview(productId, rating, comment)
                val reviews = repo.getReviews(productId)
                _detailState.update {
                    it.copy(
                        reviews = reviews,
                        hasReviewed = true,
                        canReview = true,
                        isSubmittingReview = false,
                        reviewSuccess = true,
                    )
                }
            } catch (e: Exception) {
                _detailState.update { it.copy(isSubmittingReview = false, error = e.message) }
            }
        }
    }

    fun deleteReview(productId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(error = null) }
            try {
                repo.deleteReview(productId)
                val reviews = repo.getReviews(productId)
                _detailState.update {
                    it.copy(
                        reviews = reviews,
                        hasReviewed = false,
                    )
                }
            } catch (e: Exception) {
                _detailState.update { it.copy(error = e.message) }
            }
        }
    }

    // ── Wishlist ──────────────────────────────────────────────────────────────

    private val _wishlistState = MutableStateFlow(WishlistUiState())
    val wishlistState: StateFlow<WishlistUiState> = _wishlistState.asStateFlow()

    fun loadWishlist() {
        viewModelScope.launch {
            _wishlistState.update { it.copy(isLoading = true, error = null) }
            try {
                val products = repo.getSavedProducts()
                _wishlistState.update { it.copy(products = products, isLoading = false) }
            } catch (e: Exception) {
                _wishlistState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun removeFromWishlist(productId: String) {
        viewModelScope.launch {
            try {
                repo.unsaveProduct(productId)
                _wishlistState.update { s ->
                    s.copy(products = s.products.filter { it.id != productId })
                }
            } catch (e: Exception) {
                _wishlistState.update { it.copy(error = e.message) }
            }
        }
    }
}