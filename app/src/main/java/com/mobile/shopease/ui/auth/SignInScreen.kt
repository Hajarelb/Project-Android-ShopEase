package com.mobile.shopease.ui.auth

import androidx.compose.foundation.layout.*
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
fun SignInScreen(//These are callback functions.
    onNavigateToSignUp: () -> Unit,//Called when the user presses: “Don’t have an account?”
    onSignInSuccess: () -> Unit//Called after successful login. Usually used for navigation.
) {
    val viewModel: AuthViewModel = viewModel()//Creates/connects the authentication ViewModel

    var email by remember { mutableStateOf("") }//Creates a variable that stores the email entered by the user

    var password by remember { mutableStateOf("") }//Stores password entered by the user

    val state by viewModel.authState//Gets current authentication state from ViewModel

    Column(//Organizes UI vertically
        modifier = Modifier
            .fillMaxSize()//Makes Column take the whole screen
            .padding(16.dp),//Adds space around UI

        horizontalAlignment = Alignment.CenterHorizontally,//Centers content horizontally

        verticalArrangement = Arrangement.Center//Centers content vertically
    ) {

        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium
        )//Screen title

        Spacer(modifier = Modifier.height(16.dp))//Adds vertical spacing

        TextField(
            value = email,
            onValueChange = { email = it },//Updates email state when user types
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))//Adds vertical spacing

        TextField(
            value = password,
            onValueChange = { password = it },//Updates password state
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),//Masks password characters
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Checks current authentication state
        when (state) {

            is AuthState.Loading -> {
                CircularProgressIndicator()//Shows loading spinner while authentication is running
            }

            is AuthState.Error -> {

                Text(
                    text = (state as AuthState.Error).message,//Displays error message
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        viewModel.signIn(email, password)
                    },//Retries login
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            }

            else -> {

                Button(
                    onClick = {
                        viewModel.signIn(email, password)
                    },//Starts sign in process
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In")
                }
            }
        }

        TextButton(
            onClick = onNavigateToSignUp//Navigates to signup screen
        ) {
            Text("Don't have an account? Sign Up")
        }

        //Handle navigation on successful login
        LaunchedEffect(state) {//This is a Compose side-effect. It runs code whenever state changes.

            if (state is AuthState.Success) {

                onSignInSuccess()//Navigate to next screen
            }
        }
    }
}