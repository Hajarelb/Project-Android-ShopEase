package com.mobile.shopease.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "Terms of Service",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Effective date: January 2025",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            TermsSection(
                title = "1. Acceptance of Terms",
                body = "By accessing and using ShopEase, you accept and agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use our application."
            )
            TermsSection(
                title = "2. Use of the Service",
                body = "You agree to use ShopEase only for lawful purposes and in a manner that does not infringe the rights of others. You must not misuse our services, attempt to gain unauthorized access, or transmit harmful content."
            )
            TermsSection(
                title = "3. Account Responsibilities",
                body = "You are responsible for maintaining the confidentiality of your account credentials and for all activities that occur under your account. Notify us immediately of any unauthorized use of your account."
            )
            TermsSection(
                title = "4. Orders and Payments",
                body = "By placing an order, you agree to pay the listed price plus any applicable taxes and shipping fees. We reserve the right to cancel orders at our discretion, in which case a full refund will be issued."
            )
            TermsSection(
                title = "5. Returns and Refunds",
                body = "Items may be returned within 30 days of delivery in their original condition. Refunds will be processed within 5–10 business days after we receive the returned item."
            )
            TermsSection(
                title = "6. Intellectual Property",
                body = "All content on ShopEase, including text, images, logos, and software, is the property of ShopEase or its licensors. You may not reproduce or distribute any content without our prior written consent."
            )
            TermsSection(
                title = "7. Limitation of Liability",
                body = "ShopEase shall not be liable for any indirect, incidental, or consequential damages arising from your use of the service. Our total liability shall not exceed the amount paid for the specific transaction giving rise to the claim."
            )
            TermsSection(
                title = "8. Changes to Terms",
                body = "We reserve the right to modify these Terms at any time. We will notify you of significant changes via email or in-app notification. Continued use of the service after changes constitutes acceptance of the new terms."
            )
        }
    }
}

@Composable
private fun TermsSection(title: String, body: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
