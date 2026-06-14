package com.mobile.shopease.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.repository.CartRepository
import com.mobile.shopease.data.tables.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingToCart: Boolean = false,
    val error: String? = null,
    val message: String? = null
) {
    val total: Double
        get() = items.sumOf { (it.products?.price ?: 0.0) * it.quantity }

    val itemCount: Int
        get() = items.sumOf { it.quantity }
}

class CartViewModel : ViewModel() {

    private val repository = CartRepository()

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = repository.getCartItems()
                _uiState.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCart = true, error = null) }
            try {
                repository.addToCart(productId)
                _uiState.update {
                    it.copy(
                        isAddingToCart = false,
                        message = "Product added to cart"
                    )
                }
                loadCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(isAddingToCart = false, error = e.message) }
            }
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            try {
                repository.updateQuantity(item.productId, item.quantity + 1)
                loadCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            try {
                if (item.quantity <= 1) {
                    repository.removeFromCart(item.productId)
                } else {
                    repository.updateQuantity(item.productId, item.quantity - 1)
                }
                loadCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            try {
                repository.removeFromCart(item.productId)
                loadCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /** Réinitialise message/erreur après affichage (ex: Snackbar) */
    fun clearFeedback() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
