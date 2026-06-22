package com.mobile.shopease.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.mobile.shopease.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel = viewModel(),
    localizationViewModel: LocalizationViewModel = viewModel(),
    onOrderPlaced: () -> Unit,
    onBack: () -> Unit
) {
    val state by cartViewModel.uiState.collectAsState()
    val currency by localizationViewModel.currency.collectAsState()
    val rates by localizationViewModel.exchangeRates.collectAsState()
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkout)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.total_amount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val convertedTotal = state.finalTotal * (rates[currency] ?: 1.0)
                            Text(
                                "${"%.2f".format(convertedTotal)} ${currency.uppercase()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            stringResource(R.string.item_count, state.itemCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { cartViewModel.placeOrder() },
                        enabled = state.canPlaceOrder,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isPlacingOrder) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (state.paymentMethod == "online") 
                                        stringResource(R.string.processing_payment) 
                                    else 
                                        stringResource(R.string.placing_order)
                                )
                            }
                        } else {
                            Text(stringResource(R.string.place_order), style = MaterialTheme.typography.titleMedium)
                        }
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
            Text(stringResource(R.string.promo_code), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = promoInput,
                    onValueChange = { promoInput = it },
                    label = { Text(stringResource(R.string.enter_code)) },
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
                        Text(stringResource(R.string.apply))
                    }
                }
            }
            if (state.discountPercent > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.discount_applied, state.discountPercent.toInt(), state.appliedPromoCode ?: ""),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Shipping info ---
            Text(stringResource(R.string.shipping_information), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            if (state.addresses.isNotEmpty()) {
                Text(stringResource(R.string.select_saved_address), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                state.addresses.forEach { address ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cartViewModel.selectAddress(address.id) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = state.selectedAddressId == address.id,
                            onClick = { cartViewModel.selectAddress(address.id) }
                        )
                        Column {
                            Text(address.fullName, fontWeight = FontWeight.Bold)
                            Text("${address.street}, ${address.city}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { cartViewModel.selectAddress(null) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = state.selectedAddressId == null,
                        onClick = { cartViewModel.selectAddress(null) }
                    )
                    Text(stringResource(R.string.enter_new_address), style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (state.selectedAddressId == null) {
                OutlinedTextField(
                    value = state.shippingInfo.fullName,
                    onValueChange = {
                        cartViewModel.updateShippingInfo(state.shippingInfo.copy(fullName = it))
                    },
                    label = { Text(stringResource(R.string.full_name)) },
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
                    label = { Text(stringResource(R.string.phone_number)) },
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
                    label = { Text(stringResource(R.string.delivery_address)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Payment method ---
            Text(stringResource(R.string.payment_method), style = MaterialTheme.typography.titleSmall)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { cartViewModel.setPaymentMethod("cod") }
            ) {
                RadioButton(
                    selected = state.paymentMethod == "cod",
                    onClick = { cartViewModel.setPaymentMethod("cod") }
                )
                Text(stringResource(R.string.cash_on_delivery))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { cartViewModel.setPaymentMethod("online") }
            ) {
                RadioButton(
                    selected = state.paymentMethod == "online",
                    onClick = { cartViewModel.setPaymentMethod("online") }
                )
                Text(stringResource(R.string.pay_online))
            }

            // --- Card details (only if online) ---
            if (state.paymentMethod == "online") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.card_details), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.paymentInfo.cardholderName,
                    onValueChange = {
                        cartViewModel.updatePaymentInfo(state.paymentInfo.copy(cardholderName = it))
                    },
                    label = { Text(stringResource(R.string.cardholder_name)) },
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
                    label = { Text(stringResource(R.string.card_number)) },
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
                        label = { Text(stringResource(R.string.expiry_date)) },
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
                        label = { Text(stringResource(R.string.cvv)) },
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
