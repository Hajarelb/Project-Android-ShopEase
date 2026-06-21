package com.mobile.shopease.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.ui.auth.SignInScreen
import com.mobile.shopease.ui.auth.SignUpScreen
import com.mobile.shopease.ui.screens.*
import io.github.jan.supabase.gotrue.auth

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.SignIn.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
                currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id,
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
            val cartViewModel: CartViewModel = viewModel()
            CartScreen(
                viewModel = cartViewModel,
                onProceedToCheckout = { navController.navigate(Screen.Checkout.route) }
            )
        }

        composable(Screen.Checkout.route) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Screen.Cart.route)
            }
            val cartViewModel: CartViewModel = viewModel(parentEntry)
            CheckoutScreen(
                cartViewModel = cartViewModel,
                onOrderPlaced = {
                    navController.popBackStack(Screen.ProductList.route, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToMyOrders = { navController.navigate(Screen.MyOrders.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.MyOrders.route) {
            MyOrdersScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
