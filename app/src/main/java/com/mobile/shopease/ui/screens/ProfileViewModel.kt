package com.mobile.shopease.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.repository.OrderRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val orderCount: Int = 0,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false
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
            
            val avatarUrl = user?.userMetadata
                ?.get("avatar_url")
                ?.toString()
                ?.trim('"')

            val orderCount = try {
                orderRepository.getUserOrders().size
            } catch (e: Exception) { 0 }

            _state.value = _state.value.copy(
                fullName = fullName,
                email = email,
                avatarUrl = avatarUrl,
                orderCount = orderCount,
                isLoading = false
            )
        }
    }

    fun uploadAvatar(byteArray: ByteArray) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true)
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull() ?: return@launch
                val fileName = "avatars/${user.id}.jpg"
                val bucket = SupabaseClient.client.storage["avatars"]
                
                bucket.upload(fileName, byteArray, upsert = true)
                val publicUrl = bucket.publicUrl(fileName)

                SupabaseClient.client.auth.updateUser {
                    data = buildJsonObject {
                        user.userMetadata?.forEach { (key, value) ->
                            put(key, value)
                        }
                        put("avatar_url", publicUrl)
                    }
                }
                
                _state.value = _state.value.copy(avatarUrl = publicUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isUploading = false)
            }
        }
    }
}
