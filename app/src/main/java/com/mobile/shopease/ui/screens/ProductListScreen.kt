package com.mobile.shopease.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.mobile.shopease.R
import com.mobile.shopease.data.tables.Category
import com.mobile.shopease.data.tables.Product
import com.mobile.shopease.ui.theme.StarColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onProductClick: (String) -> Unit,
    onWishlistClick: () -> Unit,
    viewModel: ProductViewModel = viewModel(),
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadProductList() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onWishlistClick) {
                            Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.wishlist))
                        }
                    }
                )
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.searchProducts(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_products)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // ── Category chips ─────────────────────────────────────────────
            if (state.categories.isNotEmpty()) {
                CategoryChipRow(
                    categories = state.categories,
                    selectedId = state.selectedCategoryId,
                    onSelect = { viewModel.selectCategory(it) }
                )
            }

            // ── Error banner ───────────────────────────────────────────────
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // ── Grid ───────────────────────────────────────────────────────
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.products) { product ->
                        ProductCard(
                            product = product,
                            isSaved = product.id in state.savedProductIds,
                            onClick = { onProductClick(product.id) },
                            onSaveToggle = { viewModel.toggleSave(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    categories: List<Category>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.all)) }
            )
        }
        items(categories) { cat ->
            val localizedName = when (cat.name.lowercase()) {
                "electronics" -> stringResource(R.string.category_electronics)
                "fashion" -> stringResource(R.string.category_fashion)
                "home" -> stringResource(R.string.category_home)
                "beauty" -> stringResource(R.string.category_beauty)
                else -> cat.name
            }
            FilterChip(
                selected = selectedId == cat.id,
                onClick = { onSelect(cat.id) },
                label = { Text(localizedName) }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isSaved: Boolean,
    localizationViewModel: LocalizationViewModel = viewModel(),
    onClick: () -> Unit,
    onSaveToggle: () -> Unit,
) {
    val currency by localizationViewModel.currency.collectAsState()
    val rates by localizationViewModel.exchangeRates.collectAsState()
    val convertedPrice = product.price * (rates[currency] ?: 1.0)
    val convertedOriginalPrice = product.originalPrice?.let { it * (rates[currency] ?: 1.0) }

    val discount = product.originalPrice?.let {
        if (it > product.price) ((1 - product.price / it) * 100).roundToInt() else null
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            Column {
                // Product image
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = product.name,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    // Rating row
                    product.avgRating?.let { avg ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = StarColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "%.1f".format(avg),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            product.reviewCount?.let { count ->
                                Text(
                                    text = " ($count)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    // Price row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${"%.2f".format(convertedPrice)} ${currency.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        convertedOriginalPrice?.let { orig ->
                            if (product.originalPrice!! > product.price) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${"%.2f".format(orig)} ${currency.uppercase()}",
                                    fontSize = 11.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Discount badge
            discount?.let {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(text = "-$it%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Heart button
            IconButton(
                onClick = onSaveToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isSaved) stringResource(R.string.remove_from_wishlist) else stringResource(R.string.add_to_wishlist),
                    tint = if (isSaved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}