package com.mobile.shopease.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel = viewModel(),
    onOrderPlaced: () -> Unit,
    onBack: () -> Unit
) {
    val state by cartViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearFeedback()
            if (it.contains("placed") || it.contains("successful")) {
                onOrderPlaced()
            }
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearFeedback()
        }
    }

    var promoInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    "Total: $${"%.2f".format(state.finalTotal)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("${state.itemCount} item(s)")

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { cartViewModel.placeOrder() },
                    enabled = state.canPlaceOrder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isPlacingOrder) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (state.paymentMethod == "online") "Processing payment..." else "Placing order...")
                        }
                    } else {
                        Text("Place Order")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Promo code ---
            Text("Promo code", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = promoInput,
                    onValueChange = { promoInput = it },
                    label = { Text("Enter code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { cartViewModel.applyPromoCode(promoInput) },
                    enabled = promoInput.isNotBlank() && !state.isApplyingPromo
                ) {
                    if (state.isApplyingPromo) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Apply")
                    }
                }
            }
            if (state.discountPercent > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Discount applied: -${state.discountPercent.toInt()}% (${state.appliedPromoCode})",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Shipping info ---
            Text("Shipping information", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.shippingInfo.fullName,
                onValueChange = {
                    cartViewModel.updateShippingInfo(state.shippingInfo.copy(fullName = it))
                },
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.shippingInfo.phoneNumber,
                onValueChange = {
                    if (it.all { c -> c.isDigit() || c == '+' }) {
                        cartViewModel.updateShippingInfo(state.shippingInfo.copy(phoneNumber = it))
                    }
                },
                label = { Text("Phone number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.shippingInfo.address,
                onValueChange = {
                    cartViewModel.updateShippingInfo(state.shippingInfo.copy(address = it))
                },
                label = { Text("Delivery address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Payment method ---
            Text("Payment method", style = MaterialTheme.typography.titleSmall)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { cartViewModel.setPaymentMethod("cod") }
            ) {
                RadioButton(
                    selected = state.paymentMethod == "cod",
                    onClick = { cartViewModel.setPaymentMethod("cod") }
                )
                Text("Cash on Delivery")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { cartViewModel.setPaymentMethod("online") }
            ) {
                RadioButton(
                    selected = state.paymentMethod == "online",
                    onClick = { cartViewModel.setPaymentMethod("online") }
                )
                Text("Pay Online")
            }

            // --- Card details (only if online) ---
            if (state.paymentMethod == "online") {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Card details", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.paymentInfo.cardholderName,
                    onValueChange = {
                        cartViewModel.updatePaymentInfo(state.paymentInfo.copy(cardholderName = it))
                    },
                    label = { Text("Cardholder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.paymentInfo.cardNumber,
                    onValueChange = {
                        if (it.length <= 16 && it.all { c -> c.isDigit() }) {
                            cartViewModel.updatePaymentInfo(state.paymentInfo.copy(cardNumber = it))
                        }
                    },
                    label = { Text("Card number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.paymentInfo.expiryDate,
                        onValueChange = {
                            if (it.length <= 5) {
                                cartViewModel.updatePaymentInfo(state.paymentInfo.copy(expiryDate = it))
                            }
                        },
                        label = { Text("MM/YY") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = state.paymentInfo.cvv,
                        onValueChange = {
                            if (it.length <= 3 && it.all { c -> c.isDigit() }) {
                                cartViewModel.updatePaymentInfo(state.paymentInfo.copy(cvv = it))
                            }
                        },
                        label = { Text("CVV") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
