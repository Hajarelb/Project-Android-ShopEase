package com.mobile.shopease

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobile.shopease.data.remote.SupabaseClient
import com.mobile.shopease.navigation.Screen
import com.mobile.shopease.ui.auth.SignInScreen
import com.mobile.shopease.ui.auth.SignUpScreen
import com.mobile.shopease.ui.screens.CartScreen
import com.mobile.shopease.ui.screens.CheckoutScreen
import com.mobile.shopease.ui.screens.ProductDetailScreen
import com.mobile.shopease.ui.screens.ProductListScreen
import com.mobile.shopease.ui.screens.ProfileScreen
import com.mobile.shopease.ui.screens.WishlistScreen
import androidx.compose.runtime.collectAsState
import com.mobile.shopease.data.UserPreferences
import com.mobile.shopease.ui.theme.ShopEaseTheme
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.handleDeeplinks
import androidx.core.view.WindowCompat

// ── Bottom nav descriptor ─────────────────────────────────────────────────────

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.ProductList, "Home",     Icons.Filled.Home,         Icons.Outlined.Home),
    BottomNavItem(Screen.Wishlist,    "Wishlist", Icons.Filled.Favorite,     Icons.Outlined.FavoriteBorder),
    BottomNavItem(Screen.Cart,        "Cart",     Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    BottomNavItem(Screen.Profile,     "Profile",  Icons.Filled.Person,       Icons.Outlined.Person),
)

// Routes on which the bottom bar should be visible
private val bottomBarRoutes = setOf(
    Screen.ProductList.route,
    Screen.Wishlist.route,
    Screen.Cart.route,
    Screen.Profile.route,
)

// ── Activity ──────────────────────────────────────────────────────────────────

class MainActivity : AppCompatActivity() {
    private val googleSignInCompleted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleAuthDeeplink(intent)
        
        val userPrefs = UserPreferences(this)
        
        setContent {
            val darkMode by userPrefs.darkMode.collectAsState(initial = false)
            
            ShopEaseTheme(darkTheme = darkMode) {
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
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val showBottomBar = currentDestination?.route in bottomBarRoutes

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    bottomNavItems.forEach { item ->
                                        val selected = currentDestination?.hierarchy
                                            ?.any { it.route == item.screen.route } == true

                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = {
                                                navController.navigate(item.screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = { 
                                                val label = when(item.label) {
                                                    "Home" -> getString(R.string.home)
                                                    "Wishlist" -> getString(R.string.wishlist)
                                                    "Cart" -> getString(R.string.cart)
                                                    "Profile" -> getString(R.string.profile)
                                                    else -> item.label
                                                }
                                                Text(label) 
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        com.mobile.shopease.navigation.NavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())                        )
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