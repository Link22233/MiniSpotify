package com.minispotify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.minispotify.app.ui.screens.home.HomeScreen
import com.minispotify.app.ui.screens.library.LibraryScreen
import com.minispotify.app.ui.screens.login.LoginScreen
import com.minispotify.app.ui.theme.MiniSpotifyTheme
import com.minispotify.app.ui.vm.AppViewModelFactory
import com.minispotify.app.ui.vm.HomeViewModel
import com.minispotify.app.ui.vm.LibraryViewModel
import com.minispotify.app.ui.vm.LoginViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = application as MiniSpotifyApp
            val factory = remember { AppViewModelFactory(app) }
            MiniSpotifyTheme {
                val nav = rememberNavController()
                var boot by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    boot =
                        if (app.container.tokenStore.getToken() != null) {
                            "main"
                        } else {
                            "login"
                        }
                }
                when (val start = boot) {
                    null ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    else ->
                        NavHost(navController = nav, startDestination = start) {
                            composable("login") {
                                val vm: LoginViewModel = viewModel(factory = factory)
                                val state by vm.state.collectAsStateWithLifecycle()
                                LoginScreen(
                                    state = state,
                                    onLogin = { e, p -> vm.login(e, p) },
                                    onLoggedIn = {
                                        nav.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                )
                            }
                            composable("main") {
                                MainShell(factory = factory)
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun MainShell(factory: AppViewModelFactory) {
    val homeVm: HomeViewModel = viewModel(factory = factory)
    val libraryVm: LibraryViewModel = viewModel(factory = factory)
    val playlists by homeVm.playlists.collectAsStateWithLifecycle()
    val favorites by libraryVm.favorites.collectAsStateWithLifecycle()
    val toast by homeVm.toast.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(toast) {
        val t = toast ?: return@LaunchedEffect
        snack.showSnackbar(t)
        homeVm.consumeToast()
    }

    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "首页标签") },
                    label = { Text("首页") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "资料库标签") },
                    label = { Text("资料库") },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> HomeScreen(playlists = playlists, onSync = { homeVm.syncFromServer() })
                1 ->
                    LibraryScreen(
                        favorites = favorites,
                        onToggleFavorite = { t -> libraryVm.toggleFavorite(t.remoteId, !t.isFavorite) },
                    )
            }
        }
    }
}
