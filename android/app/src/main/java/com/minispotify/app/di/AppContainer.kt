package com.minispotify.app.di

import android.content.Context
import com.minispotify.app.data.cache.MediaLruCache
import com.minispotify.app.data.local.AppDatabase
import com.minispotify.app.data.remote.NetworkModule
import com.minispotify.app.data.repo.MusicRepository
import com.minispotify.app.data.session.TokenStore

class AppContainer(
    context: Context,
) {
    val tokenStore = TokenStore(context)
    private val db = AppDatabase.build(context)
    private val okHttp = NetworkModule.okHttp(tokenStore)
    val api = NetworkModule.api(okHttp)
    val mediaLruCache = MediaLruCache()
    val musicRepository = MusicRepository(db, api)
}
