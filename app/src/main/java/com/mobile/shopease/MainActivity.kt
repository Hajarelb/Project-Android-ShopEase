package com.mobile.shopease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobile.shopease.navigation.Screen
import com.mobile.shopease.ui.auth.SignInScreen
import com.mobile.shopease.ui.auth.SignUpScreen
import com.mobile.shopease.ui.theme.ShopEaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopEaseTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.SignIn.route
                ) {
                    composable(Screen.SignIn.route) {
                        SignInScreen(
                            onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                            onSignInSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.SignIn.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.SignUp.route) {
                        SignUpScreen(
                            onNavigateToSignIn = { navController.popBackStack() },
                            onSignUpSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.SignIn.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Home.route) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            Greeting(
                                name = "ShopEase User",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShopEaseTheme {
        Greeting("Android")
    }
}

