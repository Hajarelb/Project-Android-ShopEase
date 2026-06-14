package com.mobile.shopease.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.repository.CartRepository
import com.mobile.shopease.data.repository.OrderRepository
import com.mobile.shopease.data.tables.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingToCart: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val isApplyingPromo: Boolean = false,
    val appliedPromoCode: String? = null,
    val discountPercent: Double = 0.0,
    val paymentMethod: String = "cod", // "cod" or "online"
    val error: String? = null,
    val message: String? = null
) {
    val total: Double
        get() = items.sumOf { (it.products?.price ?: 0.0) * it.quantity }

    val itemCount: Int
        get() = items.sumOf { it.quantity }

    val finalTotal: Double
        get() = total * (1 - discountPercent / 100)
}

class CartViewModel : ViewModel() {

    private val cartRepository = CartRepository()
    private val orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = cartRepository.getCartItems()
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
                cartRepository.addToCart(productId)
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
                cartRepository.updateQuantity(item.productId, item.quantity + 1)
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
                    cartRepository.removeFromCart(item.productId)
                } else {
                    cartRepository.updateQuantity(item.productId, item.quantity - 1)
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
                cartRepository.removeFromCart(item.productId)
                loadCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun setPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun applyPromoCode(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingPromo = true, error = null) }
            delay(1000)
            val discount = when (code.uppercase()) {
                "SAVE10" -> 10.0
                "WELCOME" -> 20.0
                else -> 0.0
            }
            if (discount > 0) {
                _uiState.update {
                    it.copy(
                        isApplyingPromo = false,
                        discountPercent = discount,
                        appliedPromoCode = code.uppercase(),
                        message = "Promo code applied!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isApplyingPromo = false,
                        error = "Invalid promo code"
                    )
                }
            }
        }
    }

    fun placeOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, error = null) }

            try {
                val state = _uiState.value
                val method = state.paymentMethod

                // simulation paiement en ligne
                if (method == "online") {
                    delay(2000) // simulate payment processing
                }

                orderRepository.createOrder(
                    items = state.items,
                    total = state.finalTotal,
                    paymentMethod = method
                )

                cartRepository.clearCart()
                loadCart()

                val message = if (method == "online") {
                    "Payment successful! Order placed."
                } else {
                    "Order placed. Pay on delivery."
                }

                _uiState.update {
                    it.copy(
                        isPlacingOrder = false,
                        message = message,
                        discountPercent = 0.0,
                        appliedPromoCode = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPlacingOrder = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}