package com.mobile.shopease.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobile.shopease.data.tables.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    currentUserId: String?,
    onBack: () -> Unit,
    viewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val cartState = cartViewModel.uiState

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) { viewModel.loadProductDetail(productId, currentUserId) }

    // Affiche un message (succès ou erreur) lié au panier, puis le nettoie
    LaunchedEffect(cartState.message, cartState.error) {
        cartState.message?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearFeedback()
        }
        cartState.error?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.product?.name ?: "Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.product?.let {
                        IconButton(onClick = { viewModel.toggleDetailSave(it.id) }) {
                            Icon(
                                imageVector = if (state.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (state.isSaved) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            state.product?.let { product ->
                AddToCartBar(
                    price = product.price,
                    inStock = product.stockQuantity > 0,
                    isAdding = cartState.isAddingToCart,
                    onAddToCart = { cartViewModel.addToCart(product.id) }
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val product = state.product ?: return@Scaffold

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Hero image ────────────────────────────────────────────────
            item {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                )
            }

            // ── Product info ──────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))

                    // Price
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$%.2f".format(product.price),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        product.originalPrice?.let { orig ->
                            if (orig > product.price) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "$%.2f".format(orig),
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Rating summary
                    product.avgRating?.let { avg ->
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { i ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i < avg.toInt()) Color(0xFFFFC107) else Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "%.1f".format(avg) + " (${product.reviewCount ?: 0} reviews)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Description
                    product.description?.let {
                        Spacer(Modifier.height(12.dp))
                        Text("Description", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                    }

                    // Stock notice
                    if (product.stockQuantity < 5 && product.stockQuantity > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Only ${product.stockQuantity} left!",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ── Divider ───────────────────────────────────────────────────
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // ── Reviews header ────────────────────────────────────────────
            item {
                Text(
                    "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // ── Add review form (only when authenticated + not yet reviewed)
            if (currentUserId != null && !state.hasReviewed) {
                item {
                    AddReviewForm(
                        isSubmitting = state.isSubmittingReview,
                        onSubmit = { rating, comment -> viewModel.submitReview(productId, rating, comment) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Review list ───────────────────────────────────────────────
            if (state.reviews.isEmpty()) {
                item {
                    Text(
                        "No reviews yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(state.reviews) { review ->
                    ReviewItem(review = review, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }

            // ── Error ─────────────────────────────────────────────────────
            state.error?.let {
                item {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToCartBar(
    price: Double,
    inStock: Boolean,
    isAdding: Boolean,
    onAddToCart: () -> Unit,
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$%.2f".format(price),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = onAddToCart,
                enabled = inStock && !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (inStock) "Ajouter au panier" else "Rupture de stock")
                }
            }
        }
    }
}

@Composable
private fun AddReviewForm(
    isSubmitting: Boolean,
    onSubmit: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Write a Review", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row {
                repeat(5) { i ->
                    IconButton(onClick = { rating = i + 1 }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "${i + 1} stars",
                            tint = if (i < rating) Color(0xFFFFC107) else Color.LightGray,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { if (rating > 0) onSubmit(rating, comment) },
                enabled = rating > 0 && !isSubmitting,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Submit")
            }
        }
    }
}

@Composable
private fun ReviewItem(review: Review, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { i ->
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i < review.rating) Color(0xFFFFC107) else Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            review.createdAt?.take(10)?.let { date ->
                Text(date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        review.comment?.let {
            Spacer(Modifier.height(2.dp))
            Text(it, fontSize = 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider()
    }
}