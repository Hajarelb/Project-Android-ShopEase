package com.mobile.shopease.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.repository.CartRepository
import com.mobile.shopease.data.tables.CartItem
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

    var uiState by mutableStateOf(CartUiState())
        private set

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val items = repository.getCartItems()
                uiState = uiState.copy(items = items, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isAddingToCart = true, error = null)
            try {
                repository.addToCart(productId)
                uiState = uiState.copy(
                    isAddingToCart = false,
                    message = "Produit ajouté au panier"
                )
                loadCart()
            } catch (e: Exception) {
                uiState = uiState.copy(isAddingToCart = false, error = e.message)
            }
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            try {
                repository.updateQuantity(item.productId, item.quantity + 1)
                loadCart()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
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
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            try {
                repository.removeFromCart(item.productId)
                loadCart()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    /** Réinitialise message/erreur après affichage (ex: Snackbar) */
    fun clearFeedback() {
        uiState = uiState.copy(message = null, error = null)
    }
}