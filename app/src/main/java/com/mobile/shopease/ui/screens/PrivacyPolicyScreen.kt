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
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
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
                "Privacy Policy",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Last updated: January 2025",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrivacySection(
                title = "1. Information We Collect",
                body = "We collect information you provide directly to us, such as when you create an account, make a purchase, or contact us for support. This includes your name, email address, phone number, shipping address, and payment information."
            )
            PrivacySection(
                title = "2. How We Use Your Information",
                body = "We use the information we collect to process transactions, send order confirmations and updates, respond to your requests, and improve our services. We do not sell your personal information to third parties."
            )
            PrivacySection(
                title = "3. Information Sharing",
                body = "We may share your information with third-party service providers who assist us in operating our platform, conducting our business, or servicing you — such as payment processors and delivery partners — so long as those parties agree to keep this information confidential."
            )
            PrivacySection(
                title = "4. Data Security",
                body = "We implement appropriate technical and organizational security measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction. Your data is encrypted in transit and at rest."
            )
            PrivacySection(
                title = "5. Your Rights",
                body = "You have the right to access, update, or delete your personal information at any time through your account settings. You may also contact us directly to exercise these rights."
            )
            PrivacySection(
                title = "6. Cookies",
                body = "Our app may use cookies and similar tracking technologies to enhance your experience. You can instruct your device to refuse all cookies or to indicate when a cookie is being sent."
            )
            PrivacySection(
                title = "7. Contact Us",
                body = "If you have any questions about this Privacy Policy, please contact us at support@shopease.com."
            )
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
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
