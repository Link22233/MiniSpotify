package com.minispotify.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minispotify.app.data.repo.MusicRepository
import com.minispotify.app.data.session.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: MusicRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {
    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            val r = repo.login(email.trim(), password, tokenStore)
            _state.value =
                r.fold(
                    onSuccess = { LoginUiState.Success },
                    onFailure = { LoginUiState.Error(it.message ?: "登录失败") },
                )
        }
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState

    data object Loading : LoginUiState

    data object Success : LoginUiState

    data class Error(
        val message: String,
    ) : LoginUiState
}
