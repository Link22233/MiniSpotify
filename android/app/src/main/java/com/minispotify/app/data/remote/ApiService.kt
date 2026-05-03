package com.minispotify.app.data.remote

import com.minispotify.app.data.remote.dto.LoginRequest
import com.minispotify.app.data.remote.dto.LoginResponse
import com.minispotify.app.data.remote.dto.PlaylistEnvelope
import com.minispotify.app.data.remote.dto.SyncBody
import com.minispotify.app.data.remote.dto.SyncResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/v1/playlists")
    suspend fun playlists(): PlaylistEnvelope

    @PUT("api/v1/playlists/sync")
    suspend fun syncPlaylists(@Body body: SyncBody): SyncResponse
}
