package com.invatech.oxy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun UserProfileImage(
    photoUrl: String?,
    displayName: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val initial = displayName?.trim()?.take(1)?.uppercase().orEmpty()

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNullOrBlank()) {
            ProfileFallback(initial = initial, size = size)
        } else {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    ProfileFallback(initial = initial, size = size)
                },
                error = {
                    ProfileFallback(initial = initial, size = size)
                }
            )
        }
    }
}

@Composable
private fun ProfileFallback(initial: String, size: Dp) {
    if (initial.isNotBlank()) {
        Text(
            text = initial,
            style = if (size >= 60.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
