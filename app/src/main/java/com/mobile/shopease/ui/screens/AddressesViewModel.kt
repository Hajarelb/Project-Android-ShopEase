package com.mobile.shopease.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.repository.AddressRepository
import com.mobile.shopease.data.tables.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddressesUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AddressesViewModel : ViewModel() {
    private val repository = AddressRepository()
    private val _state = MutableStateFlow(AddressesUiState())
    val state: StateFlow<AddressesUiState> = _state

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val addresses = repository.getAddresses()
                _state.value = _state.value.copy(addresses = addresses, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAddress(addressId)
                loadAddresses()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            try {
                repository.setDefaultAddress(addressId)
                loadAddresses()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
