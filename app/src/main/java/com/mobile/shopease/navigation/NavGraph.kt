package com.mobile.shopease.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobile.shopease.ui.screens.HomeScreen
import com.mobile.shopease.ui.screens.ProfileScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        // Add more screens here
    }
}