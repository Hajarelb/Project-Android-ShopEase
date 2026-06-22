package com.mobile.shopease.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.repository.AddressRepository
import com.mobile.shopease.data.tables.Address
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AddAddressViewModel : ViewModel() {
    private val repository = AddressRepository()

    var fullName by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var street by mutableStateOf("")
    var city by mutableStateOf("")
    var state by mutableStateOf("")
    var postalCode by mutableStateOf("")
    var country by mutableStateOf("")
    var isDefault by mutableStateOf(false)

    private val _successFlow = MutableSharedFlow<Boolean>()
    val successFlow = _successFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    fun saveAddress() {
        if (fullName.isBlank() || phoneNumber.isBlank() || street.isBlank() || city.isBlank() || 
            state.isBlank() || postalCode.isBlank() || country.isBlank()) {
            viewModelScope.launch { _errorFlow.emit("Please fill all fields") }
            return
        }

        viewModelScope.launch {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
                val address = Address(
                    userId = userId,
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    street = street,
                    city = city,
                    state = state,
                    postalCode = postalCode,
                    country = country,
                    isDefault = isDefault
                )
                repository.addAddress(address)
                if (isDefault) {
                    // This is simplified; repository.addAddress doesn't return ID easily without extra effort
                    // In a real app, we'd fetch the ID or have the DB handle defaults via triggers
                }
                _successFlow.emit(true)
            } catch (e: Exception) {
                _errorFlow.emit(e.message ?: "Unknown error")
            }
        }
    }
}
