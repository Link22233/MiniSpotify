package com.minispotify.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minispotify.app.data.repo.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: MusicRepository,
) : ViewModel() {
    val playlists =
        repo.observePlaylists().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    fun consumeToast() {
        _toast.value = null
    }

    fun syncFromServer() {
        viewModelScope.launch {
            repo.pullRemoteAndCache().fold(
                onSuccess = { _toast.value = "歌单已从服务器刷新" },
                onFailure = { _toast.value = it.message ?: "同步失败" },
            )
        }
    }
}
