package com.mobile.shopease.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Sort
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.shopease.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onNavigateToMyOrders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Calcul de la taille adaptative
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    val avatarSize = if (isTablet) 110.dp else 80.dp
    val nameFontSize = if (isTablet) 24.sp else 18.sp
    val emailFontSize = if (isTablet) 16.sp else 13.sp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Profile",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Open menu */ }) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) { }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with Orange Border
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(avatarSize / 2),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(if (isTablet) 24.dp else 16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.fullName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = nameFontSize,
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = state.email,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = emailFontSize
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Section
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Outlined.ShoppingBag,
                        title = "My Orders",
                        subtitle = "View history and tracking",
                        onClick = onNavigateToMyOrders
                    )
                    ProfileMenuDivider()
                    ProfileMenuItem(
                        icon = Icons.Outlined.Settings,
                        title = "Settings",
                        subtitle = "Privacy and app preferences",
                        onClick = onNavigateToSettings
                    )
                    ProfileMenuDivider()
                    ProfileMenuItem(
                        icon = Icons.Outlined.CreditCard,
                        title = "Payment Methods",
                        subtitle = "Manage cards and wallets",
                        onClick = onNavigateToPaymentMethods
                    )
                    ProfileMenuDivider()
                    ProfileMenuItem(
                        icon = Icons.Outlined.LocationOn,
                        title = "Addresses",
                        subtitle = "Manage your saved addresses",
                        onClick = onNavigateToAddresses
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        SupabaseClient.client.auth.signOut()
                        onSignOut()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Log out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
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
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, 
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ProfileMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
