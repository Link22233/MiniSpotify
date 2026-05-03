package com.minispotify.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minispotify.app.data.local.PlaylistEntity

@Composable
fun HomeScreen(
    playlists: List<PlaylistEntity>,
    onSync: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Button(
            onClick = onSync,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .semantics { contentDescription = "从服务器同步歌单" },
        ) {
            Text("同步歌单")
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(playlists, key = { it.id }) { p ->
                ListItem(
                    headlineContent = { Text(p.name) },
                    supportingContent = {
                        Text(
                            "remote: ${p.remoteId}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    modifier =
                        Modifier.semantics {
                            contentDescription = "歌单 ${p.name}"
                        },
                )
            }
        }
    }
}
