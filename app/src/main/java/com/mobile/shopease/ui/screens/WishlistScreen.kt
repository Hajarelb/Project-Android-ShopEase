package com.mobile.shopease.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.shopease.R
import com.mobile.shopease.data.tables.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onProductClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProductViewModel = viewModel(),
) {
    val state by viewModel.wishlistState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadWishlist() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_wishlist)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.products.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.wishlist_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.products, key = { it.id }) { product ->
                    WishlistProductRow(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onRemove = { viewModel.removeFromWishlist(product.id) }
                    )
                }
            }
        }

        state.error?.let {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(it) }
            }
        }
    }
}

@Composable
private fun WishlistProductRow(
    product: Product,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    // Swipe-to-dismiss wrapper
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onRemove(); true } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        // Reuse ProductCard in a single-column mode
        ProductCard(
            product = product,
            isSaved = true,
            onClick = onClick,
            onSaveToggle = onRemove
        )
    }
}