package com.mobile.shopease.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mobile.shopease.R
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToCurrency: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    localizationViewModel: LocalizationViewModel = viewModel()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val currentLanguage by localizationViewModel.language.collectAsState()
    val currentCurrency by localizationViewModel.currency.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val languageDisplay = when (currentLanguage) {
        "en" -> "English (US)"
        "fr" -> "French (France)"
        else -> "English (US)"
    }

    val currencyDisplay = when (currentCurrency) {
        "usd" -> "USD ($)"
        "eur" -> "EUR (€)"
        "gbp" -> "GBP (£)"
        else -> "USD ($)"
    }

    // Delete account confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account)) },
            text = {
                Text("This action is irreversible. All your data, orders, and preferences will be permanently removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount { success ->
                            if (success) {
                                showDeleteDialog = false
                                onAccountDeleted()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // --- APP PREFERENCES ---
            SettingsSectionLabel("APP PREFERENCES")

            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.dark_mode),
                    subtitle = "Cinematic obsidian interface",
                    checked = darkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.notifications),
                    subtitle = "Exclusive drops and alerts",
                    checked = notifications,
                    onCheckedChange = { viewModel.setNotifications(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- LOCALIZATION ---
            SettingsSectionLabel("LOCALIZATION")

            SettingsCard {
                SettingsNavigationRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.language),
                    subtitle = languageDisplay,
                    onClick = onNavigateToLanguage
                )
                SettingsDivider()
                SettingsNavigationRow(
                    icon = Icons.Outlined.AttachMoney,
                    title = stringResource(R.string.currency),
                    subtitle = currencyDisplay,
                    onClick = onNavigateToCurrency
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECURITY & SUPPORT ---
            SettingsSectionLabel("SECURITY & SUPPORT")

            SettingsCard {
                SettingsNavigationRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = stringResource(R.string.privacy_policy),
                    subtitle = null,
                    onClick = onNavigateToPrivacy
                )
                SettingsDivider()
                SettingsNavigationRow(
                    icon = Icons.Outlined.Description,
                    title = stringResource(R.string.terms_of_service),
                    subtitle = null,
                    onClick = onNavigateToTerms
                )
                SettingsDivider()
                SettingsNavigationRow(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = stringResource(R.string.help_center),
                    subtitle = null,
                    onClick = onNavigateToHelp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- DELETE ACCOUNT ---
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        stringResource(R.string.delete_account),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App version
            Text(
                text = "SHOPEASE VERSION 2.4.0 (BUILD 882)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- Reusable components ---

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}
