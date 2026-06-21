package com.mobile.shopease.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.repository.OrderRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val orderCount: Int = 0,
    val isLoading: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val orderRepository = OrderRepository()
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = SupabaseClient.client.auth.currentUserOrNull()
            val email = user?.email ?: ""
            val fullName = user?.userMetadata
                ?.get("full_name")
                ?.toString()
                ?.trim('"')
                ?: email.substringBefore("@")

            val orderCount = try {
                orderRepository.getUserOrders().size
            } catch (e: Exception) { 0 }

            _state.value = ProfileUiState(
                fullName = fullName,
                email = email,
                orderCount = orderCount,
                isLoading = false
            )
        }
    }
}