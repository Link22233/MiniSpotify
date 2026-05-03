package com.minispotify.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.minispotify.app.MiniSpotifyApp

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    app: MiniSpotifyApp,
) : ViewModelProvider.Factory {
    private val c = app.container

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(c.musicRepository, c.tokenStore) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(c.musicRepository) as T
            modelClass.isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(c.musicRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        }
}
