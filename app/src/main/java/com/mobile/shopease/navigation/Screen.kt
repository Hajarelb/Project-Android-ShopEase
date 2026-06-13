package com.mobile.shopease.navigation

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object ProductList : Screen("product_list")
    object Wishlist : Screen("wishlist")

    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(id: String) = "product_detail/$id"
    }
}