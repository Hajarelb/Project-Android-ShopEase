package com.mobile.shopease.data.remote

import com.mobile.shopease.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            host = BuildConfig.SUPABASE_AUTH_HOST
            scheme = BuildConfig.SUPABASE_AUTH_SCHEME
            autoSaveToStorage = true
            autoLoadFromStorage = true
        }
        install(Postgrest)
        install(Storage)
    }
}
