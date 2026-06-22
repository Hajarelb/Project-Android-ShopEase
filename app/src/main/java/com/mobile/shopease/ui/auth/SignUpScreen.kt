package com.mobile.shopease.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.mobile.shopease.ui.auth.viewmodel.AuthState
import com.mobile.shopease.ui.auth.viewmodel.AuthViewModel
import com.mobile.shopease.ui.components.SocialLoginButton
import com.mobile.shopease.ui.components.StyledTextField
import com.mobile.shopease.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val state by viewModel.authState

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo Section
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.premium_portal),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Header Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.create_account_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.join_shopease),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            StyledTextField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(R.string.username),
                placeholder = stringResource(R.string.username)
            )

            StyledTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email),
                placeholder = "name@domain.com"
            )

            StyledTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password),
                placeholder = stringResource(R.string.password),
                isPassword = true
            )

            StyledTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(R.string.confirm_password),
                placeholder = stringResource(R.string.password),
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                if (state is AuthState.Error) {
                    Text(
                        text = (state as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { 
                        if (password == confirmPassword) {
                            viewModel.signUp(email, password) 
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        stringResource(R.string.create_account).uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider Section
            Text(
                text = stringResource(R.string.or_continue_with),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Social Login Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SocialLoginButton(
                        text = "Google",
                        iconRes = R.drawable.ic_google,
                        onClick = { viewModel.signInWithGoogle() }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SocialLoginButton(
                        text = "Facebook",
                        iconRes = R.drawable.ic_facebook,
                        onClick = { viewModel.signInWithFacebook() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.already_have_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.sign_in),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.copyright),
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onSignUpSuccess()
        }
    }
}
