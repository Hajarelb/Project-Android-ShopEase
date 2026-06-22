package com.mobile.shopease.navigation

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object ProductList : Screen("product_list")
    object Wishlist : Screen("wishlist")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Profile : Screen("profile")
    object MyOrders : Screen("my_orders")
    object Settings : Screen("settings")
    object Language : Screen("language")
    object Currency : Screen("currency")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object HelpCenter : Screen("help_center")
    object Addresses : Screen("addresses")
    object AddAddress : Screen("add_address")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(id: String) = "product_detail/$id"
    }
    object OrderDetails : Screen("order_details/{orderId}") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
}