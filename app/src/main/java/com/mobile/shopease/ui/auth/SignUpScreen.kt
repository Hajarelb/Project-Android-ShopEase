package com.mobile.shopease.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.shopease.ui.auth.viewmodel.AuthState
import com.mobile.shopease.ui.auth.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit, // Called when user wants to go back to Login
    onSignUpSuccess: () -> Unit    // Called after account is successfully created
) {
    // 1. Connect to the logic (ViewModel)
    val viewModel: AuthViewModel = viewModel()

    // 2. Track what the user types
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 3. Observe the state (Loading, Success, or Error)
    val state by viewModel.authState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Input
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. UI Logic based on State
        when (state) {
            is AuthState.Loading -> {
                CircularProgressIndicator() // Show spinner while Supabase works
            }

            is AuthState.Error -> {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { viewModel.signUp(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            }

            else -> {
                Button(
                    onClick = { viewModel.signUp(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Up")
                }
            }
        }

        // 5. Navigation link back to Sign In
        TextButton(onClick = onNavigateToSignIn) {
            Text("Already have an account? Sign In")
        }

        // 6. Monitor for success to trigger navigation
        LaunchedEffect(state) {
            if (state is AuthState.Success) {
                onSignUpSuccess()
            }
        }
    }
}