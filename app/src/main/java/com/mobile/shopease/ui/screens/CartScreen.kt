package com.mobile.shopease.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobile.shopease.data.tables.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var promoInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Cart") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Your cart is empty")
                    }
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.items) { item ->
                                CartItemRow(
                                    item = item,
                                    onIncrease = { viewModel.increaseQuantity(item) },
                                    onDecrease = { viewModel.decreaseQuantity(item) },
                                    onRemove = { viewModel.removeItem(item) }
                                )
                            }

                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                // Promo code section
                                Text("Promo Code", style = MaterialTheme.typography.titleSmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = promoInput,
                                        onValueChange = { promoInput = it },
                                        label = { Text("Enter code") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.applyPromoCode(promoInput) },
                                        enabled = promoInput.isNotBlank() && !uiState.isApplyingPromo,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (uiState.isApplyingPromo) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text("Apply")
                                        }
                                    }
                                }

                                if (uiState.discountPercent > 0) {
                                    Text(
                                        "Discount applied: -${uiState.discountPercent.toInt()}% (${uiState.appliedPromoCode})",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Payment method section
                                Text("Payment Method", style = MaterialTheme.typography.titleSmall)
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { viewModel.setPaymentMethod("cod") }
                                    ) {
                                        RadioButton(
                                            selected = uiState.paymentMethod == "cod",
                                            onClick = { viewModel.setPaymentMethod("cod") }
                                        )
                                        Text("Cash on Delivery")
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { viewModel.setPaymentMethod("online") }
                                    ) {
                                        RadioButton(
                                            selected = uiState.paymentMethod == "online",
                                            onClick = { viewModel.setPaymentMethod("online") }
                                        )
                                        Text("Pay Online (Simulated)")
                                    }
                                }
                            }
                        }

                        // Total banner + checkout button
                        Surface(shadowElevation = 8.dp) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "Total (${uiState.itemCount} items)",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (uiState.discountPercent > 0) {
                                            Text(
                                                "Original: $${"%.2f".format(uiState.total)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        }
                                    }
                                    Text(
                                        "$${"%.2f".format(uiState.finalTotal)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.placeOrder() },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.items.isNotEmpty() && !uiState.isPlacingOrder
                                ) {
                                    if (uiState.isPlacingOrder) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (uiState.paymentMethod == "online") "Processing payment..." else "Placing order...")
                                        }
                                    } else {
                                        Text("Place Order")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.products?.imageUrl,
                contentDescription = item.products?.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.products?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "$${item.products?.price ?: 0.0}",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Contrôles de quantité
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(text = "${item.quantity}")
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}