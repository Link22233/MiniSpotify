package com.minispotify.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("session")

class TokenStore(
    context: Context,
) {
    private val ds = context.applicationContext.dataStore
    private val keyToken = stringPreferencesKey("jwt")

    val tokenFlow: Flow<String?> = ds.data.map { it[keyToken] }

    suspend fun getToken(): String? = tokenFlow.first()

    suspend fun setToken(token: String?) {
        ds.edit { p ->
            if (token == null) p.remove(keyToken) else p[keyToken] = token
        }
    }
}
