package com.mobile.shopease.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobile.shopease.ui.auth.SignInScreen
import com.mobile.shopease.ui.auth.SignUpScreen
import com.mobile.shopease.ui.screens.CartScreen
import com.mobile.shopease.ui.screens.ProductDetailScreen
import com.mobile.shopease.ui.screens.ProductListScreen
import com.mobile.shopease.ui.screens.WishlistScreen

@Composable
fun NavGraph(startDestination: String = Screen.SignIn.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.ProductList.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.ProductList.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ProductList.route) {
            ProductListScreen(
                onProductClick = { id ->
                    navController.navigate(Screen.ProductDetail.createRoute(id))
                },
                onWishlistClick = {
                    navController.navigate(Screen.Wishlist.route)
                }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                currentUserId = null, // replace with: SupabaseClient.client.auth.currentUserOrNull()?.id
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Wishlist.route) {
            WishlistScreen(
                onProductClick = { id ->
                    navController.navigate(Screen.ProductDetail.createRoute(id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen()
        }
    }
}
