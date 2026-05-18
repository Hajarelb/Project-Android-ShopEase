package com.mobile.shopease.data.repository

import androidx.compose.ui.semantics.password
import com.mobile.shopease.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepository {
    suspend fun signUp(email: String, pass: String) {
        SupabaseClient.client.auth.signUpWith(Email) {
            this.email = email
            this.password = pass
        }
    }

    suspend fun signIn(email: String, pass: String) {
        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email
            this.password = pass
        }
    }
}