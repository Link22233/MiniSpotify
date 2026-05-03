package com.minispotify.app.ui.screens.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minispotify.app.data.local.TrackEntity

@Composable
fun LibraryScreen(
    favorites: List<TrackEntity>,
    onToggleFavorite: (TrackEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        items(favorites, key = { it.id }) { t ->
            ListItem(
                headlineContent = { Text(t.title) },
                supportingContent = { Text(t.artist ?: "") },
                trailingContent = {
                    IconButton(
                        onClick = { onToggleFavorite(t) },
                        modifier =
                            Modifier.semantics {
                                contentDescription = if (t.isFavorite) "取消收藏 ${t.title}" else "收藏 ${t.title}"
                            },
                    ) {
                        Text(if (t.isFavorite) "★" else "☆")
                    }
                },
            )
        }
    }
}
