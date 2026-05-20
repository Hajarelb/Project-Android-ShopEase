package com.mobile.shopease.data.repository

import com.mobile.shopease.BuildConfig
import com.mobile.shopease.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Facebook
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepository {
    private val oauthRedirectUrl =
        "${BuildConfig.SUPABASE_AUTH_SCHEME}://${BuildConfig.SUPABASE_AUTH_HOST}"

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

    suspend fun signInWithGoogle() {
        SupabaseClient.client.auth.signInWith(Google, redirectUrl = oauthRedirectUrl)
    }

    suspend fun signInWithFacebook() {
        SupabaseClient.client.auth.signInWith(Facebook, redirectUrl = oauthRedirectUrl)
    }
}
