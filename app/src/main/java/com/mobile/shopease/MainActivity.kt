package com.mobile.shopease

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.navigation.Screen
import com.mobile.shopease.ui.auth.SignInScreen
import com.mobile.shopease.ui.auth.SignUpScreen
import com.mobile.shopease.ui.screens.ProductDetailScreen
import com.mobile.shopease.ui.screens.ProductListScreen
import com.mobile.shopease.ui.screens.WishlistScreen
import com.mobile.shopease.ui.theme.ShopEaseTheme
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.handleDeeplinks

class MainActivity : ComponentActivity() {
    private val googleSignInCompleted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAuthDeeplink(intent)
        setContent {
            ShopEaseTheme {
                val navController = rememberNavController()
                var isReady by remember { mutableStateOf(false) }
                var startDestination by remember { mutableStateOf(Screen.SignIn.route) }

                LaunchedEffect(Unit) {
                    SupabaseClient.client.auth.loadFromStorage()
                    if (SupabaseClient.client.auth.currentSessionOrNull() != null) {
                        startDestination = Screen.ProductList.route
                    }
                    isReady = true
                }

                LaunchedEffect(googleSignInCompleted.value) {
                    if (googleSignInCompleted.value) {
                        navController.navigate(Screen.ProductList.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                            launchSingleTop = true
                        }
                        googleSignInCompleted.value = false
                    }
                }

                if (!isReady) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Screen.SignIn.route) {
                            SignInScreen(
                                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                                onSignInSuccess = {
                                    navController.navigate(Screen.ProductList.route) {
                                        popUpTo(Screen.SignIn.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.SignUp.route) {
                            SignUpScreen(
                                onNavigateToSignIn = { navController.popBackStack() },
                                onSignUpSuccess = {
                                    navController.navigate(Screen.ProductList.route) {
                                        popUpTo(Screen.SignIn.route) { inclusive = true }
                                    }
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
                            val productId = backStackEntry.arguments?.getString("productId")
                                ?: return@composable
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
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthDeeplink(intent)
    }

    private fun handleAuthDeeplink(intent: Intent) {
        SupabaseClient.client.handleDeeplinks(intent) {
            googleSignInCompleted.value = true
        }
    }
}