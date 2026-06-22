package com.mobile.shopease.data.repository

import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.data.tables.Address
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddressRepository {
    private val client = SupabaseClient.client

    suspend fun getAddresses(): List<Address> = withContext(Dispatchers.IO) {
        val userId = client.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
        client.postgrest["addresses"]
            .select(columns = Columns.ALL) {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<Address>()
    }

    suspend fun addAddress(address: Address) = withContext(Dispatchers.IO) {
        client.postgrest["addresses"].insert(address)
    }

    suspend fun updateAddress(address: Address) = withContext(Dispatchers.IO) {
        client.postgrest["addresses"].update(address) {
            filter {
                eq("id", address.id ?: "")
            }
        }
    }

    suspend fun deleteAddress(addressId: String) = withContext(Dispatchers.IO) {
        client.postgrest["addresses"].delete {
            filter {
                eq("id", addressId)
            }
        }
    }

    suspend fun setDefaultAddress(addressId: String) = withContext(Dispatchers.IO) {
        val userId = client.auth.currentUserOrNull()?.id ?: return@withContext
        
        // Reset all addresses to not default
        client.postgrest["addresses"].update(mapOf("is_default" to false)) {
            filter {
                eq("user_id", userId)
            }
        }
        
        // Set specific address to default
        client.postgrest["addresses"].update(mapOf("is_default" to true)) {
            filter {
                eq("id", addressId)
            }
        }
    }
}
