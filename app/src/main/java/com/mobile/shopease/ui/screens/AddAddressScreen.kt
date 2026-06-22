package com.mobile.shopease.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.shopease.R
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddAddressViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.successFlow.collectLatest { success ->
            if (success) {
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorFlow.collectLatest { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_new_address)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.fullName,
                onValueChange = { viewModel.fullName = it },
                label = { Text(stringResource(R.string.full_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.phoneNumber,
                onValueChange = { viewModel.phoneNumber = it },
                label = { Text(stringResource(R.string.phone_number)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = viewModel.street,
                onValueChange = { viewModel.street = it },
                label = { Text(stringResource(R.string.street_address)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.city,
                    onValueChange = { viewModel.city = it },
                    label = { Text(stringResource(R.string.city)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.state,
                    onValueChange = { viewModel.state = it },
                    label = { Text(stringResource(R.string.state_province)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.postalCode,
                    onValueChange = { viewModel.postalCode = it },
                    label = { Text(stringResource(R.string.postal_code)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.country,
                    onValueChange = { viewModel.country = it },
                    label = { Text(stringResource(R.string.country)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = viewModel.isDefault,
                    onCheckedChange = { viewModel.isDefault = it }
                )
                Text(stringResource(R.string.set_as_default_address))
            }

            Button(
                onClick = { viewModel.saveAddress() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save_address))
            }
        }
    }
}
