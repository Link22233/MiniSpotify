package com.minispotify.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    val user: UserDto,
)

data class UserDto(
    val id: Long,
    val email: String?,
    @SerializedName("displayName") val displayName: String?,
)

data class PlaylistEnvelope(
    val playlists: List<PlaylistDto>,
)

data class PlaylistDto(
    val id: Long? = null,
    @SerializedName("remoteId") val remoteId: String,
    val name: String,
    @SerializedName("updatedAt") val updatedAt: String?,
    val tracks: List<TrackDto> = emptyList(),
)

data class TrackDto(
    @SerializedName("remoteId") val remoteId: String,
    val title: String,
    val artist: String? = null,
    @SerializedName("durationMs") val durationMs: Long? = null,
    @SerializedName("streamUrl") val streamUrl: String? = null,
)

data class SyncBody(
    val playlists: List<PlaylistDto>,
)

data class SyncResponse(
    val ok: Boolean,
    val synced: Int,
)
