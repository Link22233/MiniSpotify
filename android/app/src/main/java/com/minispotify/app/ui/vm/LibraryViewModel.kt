package com.minispotify.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minispotify.app.data.repo.MusicRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repo: MusicRepository,
) : ViewModel() {
    val favorites =
        repo.observeFavorites().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    fun toggleFavorite(
        remoteId: String,
        favorite: Boolean,
    ) {
        viewModelScope.launch {
            repo.toggleFavorite(remoteId, favorite)
        }
    }
}
