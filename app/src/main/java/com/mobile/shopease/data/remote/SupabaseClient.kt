package com.mobile.shopease.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://lihiauloblhmggyumine.supabase.co/rest/v1/",
        supabaseKey = "YOeyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpaGlhdWxvYmxobWdneXVtaW5lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkxMTQ5NzUsImV4cCI6MjA5NDY5MDk3NX0.5EaxuRrEAiopFgYmtAWK4EwuVwIP5SUhQPwhE5Ju4DQ"
    ) {
        install(Auth)
        install(Postgrest)
    }
}